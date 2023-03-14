package com.muratozturk.metflix.ui.person.movies

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.muratozturk.metflix.R
import com.muratozturk.metflix.common.*
import com.muratozturk.metflix.common.enums.MediaTypeEnum
import com.muratozturk.metflix.databinding.FragmentPersonMoviesBinding
import com.muratozturk.metflix.ui.person.PersonDetailFragmentDirections
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class PersonMoviesFragment : Fragment(R.layout.fragment_person_movies) {
    private val binding by viewBinding(FragmentPersonMoviesBinding::bind)
    private val viewModel: PersonMoviesViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        collectData()
    }

    fun initUI() {
        with(binding) {
            with(viewModel) {

            }
        }
    }

    private fun onClick(id: Int) {
        val action = PersonDetailFragmentDirections.actionPersonDetailFragmentToDetailsFragment(
            id,
            MediaTypeEnum.MOVIE
        )
        findNavController().navigate(action)
    }

    fun collectData() {
        with(viewModel) {
            with(binding) {

                lifecycleScope.launchWhenCreated {
                    movies.collectLatest { response ->
                        when (response) {
                            is Resource.Loading -> {
                                recyclerViewMovies.gone()
                            }
                            is Resource.Error -> {
                                recyclerViewMovies.gone()

                                requireActivity().showToast(
                                    getString(com.muratozturk.metflix.R.string.error),
                                    response.throwable.localizedMessage ?: "Error",
                                    www.sanju.motiontoast.MotionToastStyle.ERROR
                                )

                            }
                            is Resource.Success -> {
                                recyclerViewMovies.visible()

                                val imagesAdapter =
                                    PersonMoviesAdapter(response.data)
                                recyclerViewMovies.adapter = imagesAdapter
                                imagesAdapter.onClick = ::onClick

                            }
                        }

                    }
                }


            }
        }
    }

    companion object {
        fun createBundle(id: Int) =
            PersonMoviesFragment().apply {
                arguments = Bundle().apply {
                    putInt(Constants.Arguments.ID, id)
                }
            }
    }

}