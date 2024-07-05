package com.dilekcelebi.myapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dilekcelebi.myapp.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmHolder> {

    ArrayList<Film> filmArrayList;

    public FilmAdapter(ArrayList<Film> filmArrayList){
        this.filmArrayList = filmArrayList;

    }

    @NonNull
    @Override
    public FilmHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FilmHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmHolder holder, int position) {
        holder.binding.recyclerViewTextView.setText(filmArrayList.get(position).filmName);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), DetailsActivity.class);
                intent.putExtra("info", "old");
                intent.putExtra("filmId", filmArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);


            }
        });
    }

    @Override
    public int getItemCount() {
        return filmArrayList.size();
    }

    public class FilmHolder extends RecyclerView.ViewHolder {
        private RecyclerRowBinding binding;

        public FilmHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
