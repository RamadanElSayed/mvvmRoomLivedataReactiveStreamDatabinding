/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.observability.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import com.example.android.observability.Injection;
import com.example.android.persistence.R;
import com.example.android.persistence.databinding.ActivityUserBinding;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Main screen of the app. Displays a user name and gives the option to update the user name.
 */
public class UserActivity extends AppCompatActivity implements ItemClicked {

    private static final String TAG = UserActivity.class.getSimpleName();
    private TextView mUserName;
    private ViewModelFactory mViewModelFactory;
    private UserViewModel mViewModel;
    private ActivityUserBinding activityUserBinding;
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityUserBinding= DataBindingUtil.setContentView(this,R.layout.activity_user);
        activityUserBinding.setView(this);
        mUserName = activityUserBinding.userName;
        mViewModelFactory = Injection.provideViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(UserViewModel.class);
        activityUserBinding.setViewModel(mViewModel);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Subscribe to the emissions of the user name from the view model.
        // Update the user name text view, at every onNext emission.
        // In case of error, log the exception.
        /*mDisposable.add(mViewModel.getUserName()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userName -> mUserName.setText(userName),
                        throwable -> Log.e(TAG, "Unable to update username", throwable)));*/
        mViewModel.getUserNameFlowable().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                mUserName.setText(s);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        // clear all the subscriptions
        mDisposable.clear();
    }

    private void updateUserName() {
        // Disable the update button until the user name update has been done
        activityUserBinding.updateUser.setEnabled(false);
        // Subscribe to updating the user name.
        // Re-enable the button once the user name has been updated
        mDisposable.add(mViewModel.updateUserName()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() ->  activityUserBinding.updateUser.setEnabled(true),
                        throwable -> Log.e(TAG, "Unable to update username", throwable)));
    }

    @Override
    public void onUpdateClicked() {
        updateUserName();
    }
}
