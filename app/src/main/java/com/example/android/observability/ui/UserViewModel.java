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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableField;

import com.example.android.observability.UserDataSource;
import com.example.android.observability.persistence.User;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.functions.Action;

/**
 * View Model for the {@link UserActivity}
 */
public class UserViewModel extends ViewModel {

    private final UserDataSource mDataSource;

    private User mUser;
    public ObservableField<String> name;
    public UserViewModel(UserDataSource dataSource) {
        mDataSource = dataSource;
        name=new ObservableField<>();
    }

    /**
     * Get the user name of the user.
     *
     * @return a {@link Flowable} that will emit every time the user name has been updated.
     */
    public Flowable<String> getUserName() {
        return mDataSource.getUser()
                // for every emission of the user, get the user name
                .map(user -> {
                    mUser = user;
                    return user.getUserName();
                });

    }

    public LiveData<String> getUserNameFlowable() {

        return LiveDataReactiveStreams.fromPublisher(mDataSource.getUser()
                // for every emission of the user, get the user name
                .map(user -> {
                    mUser = user;
                    return user.getUserName();
                }));

    }

//    /**
//     * Update the user name.
//     *
//     * @param   new user name
//     * @return a {@link Completable} that completes when the user name is updated
//     */
    public Completable updateUserName() {
        String userName=name.get();
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                // if there's no use, create a new user.
                // if we already have a user, then, since the user object is immutable,
                // create a new user, with the id of the previous user and the updated user name.
                mUser = mUser == null
                        ? new User(userName)
                        : new User(mUser.getId(), userName);

                mDataSource.insertOrUpdateUser(mUser);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        name=null;
    }
}
