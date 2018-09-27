package com.codingwithmitch.applicationone.models;

import android.os.Parcel;
import android.os.Parcelable;

public class AudioTrack implements Parcelable{

    private String title;
    private String url;

    public AudioTrack(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public AudioTrack() {

    }

    protected AudioTrack(Parcel in) {
        title = in.readString();
        url = in.readString();
    }

    public static final Creator<AudioTrack> CREATOR = new Creator<AudioTrack>() {
        @Override
        public AudioTrack createFromParcel(Parcel in) {
            return new AudioTrack(in);
        }

        @Override
        public AudioTrack[] newArray(int size) {
            return new AudioTrack[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "AudioTrack{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(url);
    }
}
