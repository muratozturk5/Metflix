package com.muratozturk.metflix.di

import android.content.Context
import android.content.SharedPreferences
import com.muratozturk.metflix.common.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object SharedPreferenceModule {

    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
    }

}
