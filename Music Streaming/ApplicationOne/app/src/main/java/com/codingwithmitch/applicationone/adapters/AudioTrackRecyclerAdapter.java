package com.codingwithmitch.applicationone.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codingwithmitch.applicationone.R;
import com.codingwithmitch.applicationone.models.AudioTrack;

import java.util.ArrayList;

public class AudioTrackRecyclerAdapter extends RecyclerView.Adapter<AudioTrackRecyclerAdapter.ViewHolder> {


    private AudioTrackClickListener mAudioTrackClickListener;
    private ArrayList<AudioTrack> mAudioTracks = new ArrayList<>();

    public AudioTrackRecyclerAdapter(AudioTrackClickListener mAudioTrackClickListener, ArrayList<AudioTrack> mAudioTracks) {
        this.mAudioTrackClickListener = mAudioTrackClickListener;
        this.mAudioTracks = mAudioTracks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_audio_track, parent, false);
        final ViewHolder holder = new ViewHolder(view, mAudioTrackClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(mAudioTracks.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return mAudioTracks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        AudioTrackClickListener mAudioTrackClickListener;
        TextView title;

        public ViewHolder(View itemView, AudioTrackClickListener clickListener) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            mAudioTrackClickListener = clickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mAudioTrackClickListener.onTrackSelected(getAdapterPosition());
        }
    }

    public interface AudioTrackClickListener{
        void onTrackSelected(int position);
    }
}







