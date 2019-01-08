package com.lyc.newtestapplication.newtestapplication.ViewModelTest.ui.viewmodeltest;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.lyc.newtestapplication.newtestapplication.R;
import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;


public class ViewModelTestFragment extends Fragment {

    private static final String TAG ="ViewModelTestFragment" ;
    private ViewModelTestViewModel mViewModel;

    public static ViewModelTestFragment newInstance() {
        return new ViewModelTestFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.view_model_test_fragment, container, false);
        TextView textView=view.findViewById(R.id.message);

//        Observable.just("Hello world!").subscribe(new Observer<String>() {
//
//            //这是新加入的方法，在订阅后发送数据之前，
//            //回首先调用这个方法，而Disposable可用于取消订阅
//            @Override
//            public void onSubscribe(Disposable d) {
//
//                Log.i(TAG," call onSubscribe");
////                d.dispose();
//            }
//
//            @Override
//            public void onNext(String s) {
//
//                Log.i(TAG," the message is "+s);
//
//                Log.i(TAG," call onNext");
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Log.i(TAG," call onError");
//            }
//
//            @Override
//            public void onComplete() {
//                Log.i(TAG," call onComplete");
//            }
//        });


        String[] sArray={"item1","item2","item3"};

        Flowable.fromArray(sArray).subscribe(new Subscriber<String>() {

            //当订阅后，会首先调用这个方法，其实就相当于onStart()，
            //传入的Subscription s参数可以用于请求数据或者取消订阅
            @Override
            public void onSubscribe(Subscription s) {

                Log.i(TAG," call onSubscribe");

//                s.cancel();
            }

            @Override
            public void onNext(String s) {

                Log.i(TAG," the message is "+s);

                Log.i(TAG," call onNext");
            }

            @Override
            public void onError(Throwable t) {
                Log.i(TAG," call onError");
            }

            @Override
            public void onComplete() {
                Log.i(TAG," call onComplete");
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ViewModelTestViewModel.class);
        // TODO: Use the ViewModel
    }

}
