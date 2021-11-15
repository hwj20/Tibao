package com.example.tibao;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

    private String[] localDataSet;
    private Context mContext;
    private String[] localAnswer;
    private int[] imageId;


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = (TextView) view.findViewById(R.id.item_news);
            imageView = view.findViewById(R.id.item_pic);
        }

        public TextView getTextView() {
            return textView;
        }
        public ImageView getImageView(){
            return imageView;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public RecycleViewAdapter(String[] dataSet, String[] answers, int[] imageId, Context context) {
        localDataSet = dataSet;
        localAnswer = answers;
        this.imageId = imageId;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public @NotNull ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_news, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        viewHolder.getTextView().setText(localDataSet[position]);
//        int cameraId = mContext.getResources().getIdentifier("tibao:drawable/" + "doge"+(position)+".jpeg", null, null);
        viewHolder.getImageView().setImageResource(imageId[position]);

        viewHolder.getTextView().setOnClickListener(
                view -> UIutils.showWindow(localDataSet[position],localAnswer[position], mContext)
        );
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.length;
    }
}
