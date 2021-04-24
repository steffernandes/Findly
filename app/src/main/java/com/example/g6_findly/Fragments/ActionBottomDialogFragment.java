package com.example.g6_findly.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.g6_findly.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class ActionBottomDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener{

    public static final String TAG = "ActionBottomDialog";
    private ItemClickListener mListener;
    public static Context context;

    public static ActionBottomDialogFragment newInstance() {
        return new ActionBottomDialogFragment();
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet, container, false);
    }
/*
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView take_photo = view.findViewById(R.id.textView);

        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick("take_picture");
                dismiss();
            }
        });

        final TextView gallery = view.findViewById(R.id.textView2);

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick("gallery");
                dismiss();
            }
        });

        view.findViewById(R.id.textView).setOnClickListener(this);
        view.findViewById(R.id.textView2).setOnClickListener(this);
    }
*/
@Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    view.findViewById(R.id.textView).setOnClickListener((View.OnClickListener) this);
    view.findViewById(R.id.textView2).setOnClickListener((View.OnClickListener) this);
}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof ItemClickListener) {
            mListener = (ItemClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ItemClickListener");
        }
    }
    @Override

    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override public void onClick(View view) {
        String tag = view.getTag().toString();
        if (tag=="cancel"){
            dismiss();
        }
        else{
            //TextView tvSelected = (TextView) view;
            //mListener.onItemClick(tvSelected.getText().toString());
            mListener.onItemClick(tag);
        }

    }
    public interface ItemClickListener {
        void onItemClick(String tag);
    }

}
