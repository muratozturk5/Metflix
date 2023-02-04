package com.muratozturk.metflix.ui.explore

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.muratozturk.metflix.R
import com.muratozturk.metflix.common.changeFocusedInputTint
import com.muratozturk.metflix.common.enums.MediaTypeEnum
import com.muratozturk.metflix.common.gone
import com.muratozturk.metflix.common.showToast
import com.muratozturk.metflix.common.visible
import com.muratozturk.metflix.data.model.FilterResult
import com.muratozturk.metflix.databinding.FragmentExploreBinding
import com.muratozturk.metflix.ui.home.LoadStateAdapter
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import www.sanju.motiontoast.MotionToastStyle

@AndroidEntryPoint
class ExploreFragment : Fragment(R.layout.fragment_explore) {
    private val binding by viewBinding(FragmentExploreBinding::bind)
    private val viewModel: ExploreViewModel by viewModels()
    private val movieAdapter: ExploreAdapter by lazy { ExploreAdapter(::onClickItem) }
    private val serieAdapter: ExploreSeriesAdapter by lazy { ExploreSeriesAdapter(::onClickItem) }
    private var returnFilterResult = FilterResult()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        filterResultController()
        filterResult()
        initUI()
        collectData()
    }

    private fun initUI() {
        with(binding) {
            searchEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                searchEditText.changeFocusedInputTint(hasFocus)
            }
            searchEditText.addTextChangedListener { editable ->
                editable?.let {
                    if (it.isNotEmpty()) {
                        searchController(it.toString())
                    } else {
                        filterResultController()
                    }
                }
            }
            filterButton.setOnClickListener {
                val action = ExploreFragmentDirections.actionExploreFragmentToFilterFragment(
                    returnFilterResult
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun onClickItem(id: Int) {

    }

    private fun collectData() {
        with(viewModel) {
            with(binding) {

                viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                    discoverMovies.collectLatest { response ->

                        // Set Adapter If Type Is Movie
                        if (returnFilterResult.type == MediaTypeEnum.MOVIE) {

                            // Creating Contact Adapter For Paging Footer Span Count
                            val contactAdapter = movieAdapter.withLoadStateFooter(
                                footer = LoadStateAdapter { movieAdapter.retry() }
                            )

                            recyclerDiscover.layoutManager =
                                GridLayoutManager(context, 2).apply {
                                    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                                        override fun getSpanSize(position: Int): Int {
                                            return if (contactAdapter.getItemViewType(position) in
                                                arrayOf(1)
                                            ) spanCount else 1
                                        }

                                    }
                                }

                            recyclerDiscover.adapter = contactAdapter

                            movieAdapter.submitData(lifecycle, response)


                            movieAdapter.loadStateFlow.collectLatest { loadStates ->
                                when (loadStates.refresh) {
                                    is LoadState.Loading -> {
                                        discoverLoading.visible()
                                        discoverLoading.startShimmer()
                                        recyclerDiscover.gone()
                                    }
                                    is LoadState.NotLoading -> {
                                        discoverLoading.gone()
                                        discoverLoading.stopShimmer()
                                        recyclerDiscover.visible()
                                    }
                                    is LoadState.Error -> {
                                        requireActivity().showToast(
                                            getString(R.string.error),
                                            (loadStates.refresh as LoadState.Error).error.localizedMessage
                                                ?: "Error",
                                            MotionToastStyle.ERROR
                                        )
                                    }

                                }
                            }
                        }


                    }
                }

                viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                    discoverSeries.collectLatest { response ->
                        // Set Adapter If Type Is Series
                        if (returnFilterResult.type == MediaTypeEnum.SERIE) {
                            // Creating Contact Adapter For Paging Footer Span Count
                            val contactAdapter = serieAdapter.withLoadStateFooter(
                                footer = LoadStateAdapter { serieAdapter.retry() }
                            )

                            recyclerDiscover.layoutManager =
                                GridLayoutManager(context, 2).apply {
                                    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                                        override fun getSpanSize(position: Int): Int {
                                            return if (contactAdapter.getItemViewType(position) in
                                                arrayOf(1)
                                            ) spanCount else 1
                                        }

                                    }
                                }


                            recyclerDiscover.adapter = contactAdapter



                            recyclerDiscover.adapter = contactAdapter
                            serieAdapter.submitData(lifecycle, response)

                            serieAdapter.loadStateFlow.collectLatest { loadStates ->
                                when (loadStates.refresh) {
                                    is LoadState.Loading -> {
                                        discoverLoading.visible()
                                        discoverLoading.startShimmer()
                                        recyclerDiscover.gone()
                                    }
                                    is LoadState.NotLoading -> {
                                        discoverLoading.gone()
                                        discoverLoading.stopShimmer()
                                        recyclerDiscover.visible()
                                    }
                                    is LoadState.Error -> {
                                        requireActivity().showToast(
                                            getString(R.string.error),
                                            (loadStates.refresh as LoadState.Error).error.localizedMessage
                                                ?: "Error",
                                            MotionToastStyle.ERROR
                                        )
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun fetchFilters() {
        with(binding) {
            searchEditText.text.clear()
            searchEditText.clearFocus()
            
            filtersLoading.visible()
            filtersLoading.startShimmer()

            val filterList = mutableListOf<String>()

            if (returnFilterResult.type == MediaTypeEnum.MOVIE) {
                filterList.add(resources.getString(R.string.movie))
            } else {
                filterList.add(resources.getString(R.string.tv_series))
            }

            if (returnFilterResult.includeAdult) {
                filterList.add(resources.getString(R.string.include_adult))
            }

            returnFilterResult.selectedGenreList.forEach {
                filterList.add(it.name)
            }

            val filterAdapter =
                SelectedFiltersAdapter(filterList)
            recyclerViewFilters.adapter = filterAdapter

            filtersLoading.gone()
            filtersLoading.stopShimmer()
            recyclerViewFilters.visible()

        }
    }

    private fun filterResultController() {
        if (returnFilterResult.type == MediaTypeEnum.MOVIE) {
            viewModel.getDiscoverMovies(returnFilterResult)
        } else {
            viewModel.getDiscoverSeries(returnFilterResult)
        }

        fetchFilters()
    }

    private fun searchController(query: String) {
        if (returnFilterResult.type == MediaTypeEnum.MOVIE) {
            viewModel.getSearchMovie(query, returnFilterResult.includeAdult)
        } else {
            viewModel.getSearchSerie(query, returnFilterResult.includeAdult)
        }

    }

    private fun filterResult() {
        setFragmentResultListener("popUp") { _, bundle ->

            returnFilterResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable("filterResult", FilterResult::class.java) ?: FilterResult()
            } else {
                @Suppress("DEPRECATION") (bundle.getParcelable("filterResult")
                    ?: FilterResult())
            }


            filterResultController()
        }
    }
}