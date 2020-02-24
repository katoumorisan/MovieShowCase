package com.yzl.movieshowcase.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.yzl.movieshowcase.R;
import com.yzl.movieshowcase.model.Item;

import java.util.List;

/**
 * 读取item数据适配器
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private List<Item> ItemList;
    private OnItemListener mOnItemListener;
//    private static final String IMAGE_URL_HEAD = "https://image.tmdb.org/t/p";

    public ItemAdapter(List<Item> data, OnItemListener onItemListener) {
        ItemList = data;
        mOnItemListener = onItemListener;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ItemAdapter.ViewHolder(view, mOnItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Item model = ItemList.get(position);
        if (!TextUtils.isEmpty(model.getTitle())) {
            holder.title.setText(model.getTitle());
        }
        if (!TextUtils.isEmpty(model.getYear())) {
            holder.year.setText(model.getYear());
        }
        if (!TextUtils.isEmpty(model.getImdbID())) {
            holder.imdbid.setText(model.getImdbID());
        }
        //使用毕加索进行图片的缓存和展现，设置placeholder和errorholder
        Picasso.get().load(model.getPoster())
                .fit()
                .placeholder(R.drawable.onloading)
                .error(R.drawable.error)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return ItemList != null ? ItemList.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView year;
        private TextView imdbid;
        private ImageView imageView;
        OnItemListener mOnItemListener;

        ViewHolder(@NonNull View itemView, OnItemListener OnItemListener) {
            super(itemView);
            title = itemView.findViewById(R.id.title_item_tv);
            year = itemView.findViewById(R.id.year_item_tv);
            imdbid = itemView.findViewById(R.id.imdbid_item_tv);
            imageView = itemView.findViewById(R.id.image_item_iv);
            mOnItemListener = OnItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnItemListener.OnItemClick(getAdapterPosition());
        }
    }

    public interface OnItemListener {
        void OnItemClick(int position);
    }
}
