package com.lyc.newtestapplication.newtestapplication.ViewModelTest.ui.viewmodeltest;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.lyc.newtestapplication.newtestapplication.R;


public class ViewModelTestFragment extends Fragment {

    private ViewModelTestViewModel mViewModel;

    public static ViewModelTestFragment newInstance() {
        return new ViewModelTestFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_model_test_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ViewModelTestViewModel.class);
        // TODO: Use the ViewModel
    }

}
