package android.dbmeter.net.adapter;

import android.content.Context;
import android.dbmeter.net.R;
import android.dbmeter.net.databinding.ItemFragmentMusicPlayerCategoriesBinding;
import android.dbmeter.net.model.Category;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.CategoryViewHolder>{
    private List<Category> listData;
    private Context context;
    private LayoutInflater inflater;
    private OnItemClickedListener onItemClickedListener;


    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private ItemFragmentMusicPlayerCategoriesBinding binding;

        CategoryViewHolder(ItemFragmentMusicPlayerCategoriesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickedListener {
        void onItemClick(int index);
    }

    public AdapterCategory(Context context, List<Category> list){
        this.context = context;
        this.listData = list;
    }

    @NonNull
    @Override
    public AdapterCategory.CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (inflater == null)
            inflater = LayoutInflater.from(parent.getContext());
        ItemFragmentMusicPlayerCategoriesBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.item_fragment_music_player_categories, parent,false);
        return new AdapterCategory.CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final  AdapterCategory.CategoryViewHolder holder, final int position) {
        Category category = listData.get(position);

        holder.binding.categoryTitle.setText(category.getCategoryTitle());
        Glide.with(context).load(category.getCategoryThumbnail()).into(holder.binding.categoryThumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickedListener != null){
                    onItemClickedListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    public void notify(List<Category> list){
        listData.clear();
        listData.addAll(list);
        notifyDataSetChanged();
    }
}
