package com.cleveroad.sample;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Music track model.
 */
class MusicItem implements Parcelable {
    private String title;
    private String album;
    private String artist;
    private long duration;
    private Uri albumArtUri;
    private Uri fileUri;

    public MusicItem title(String title) {
        this.title = title;
        return this;
    }

    public MusicItem album(String album) {
        this.album = album;
        return this;
    }

    public MusicItem artist(String artist) {
        this.artist = artist;
        return this;
    }

    public MusicItem duration(long duration) {
        this.duration = duration;
        return this;
    }

    public MusicItem albumArtUri(Uri albumArtUri) {
        this.albumArtUri = albumArtUri;
        return this;
    }

    public MusicItem fileUri(Uri fileUri) {
        this.fileUri = fileUri;
        return this;
    }

    public String title() {
        return title;
    }

    public String album() {
        return album;
    }

    public String artist() {
        return artist;
    }

    public long duration() {
        return duration;
    }

    public Uri albumArtUri() {
        return albumArtUri;
    }

    public Uri fileUri() {
        return fileUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MusicItem item = (MusicItem) o;

        if (duration != item.duration) return false;
        if (title != null ? !title.equals(item.title) : item.title != null) return false;
        if (album != null ? !album.equals(item.album) : item.album != null) return false;
        if (artist != null ? !artist.equals(item.artist) : item.artist != null) return false;
        if (albumArtUri != null ? !albumArtUri.equals(item.albumArtUri) : item.albumArtUri != null)
            return false;
        return fileUri != null ? fileUri.equals(item.fileUri) : item.fileUri == null;

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (album != null ? album.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        result = 31 * result + (albumArtUri != null ? albumArtUri.hashCode() : 0);
        result = 31 * result + (fileUri != null ? fileUri.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MusicItem{" +
                "title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                ", duration=" + duration +
                ", albumArtUri=" + albumArtUri +
                ", fileUri=" + fileUri +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.album);
        dest.writeString(this.artist);
        dest.writeLong(this.duration);
        dest.writeParcelable(this.albumArtUri, 0);
        dest.writeParcelable(this.fileUri, 0);
    }

    public MusicItem() {
    }

    protected MusicItem(Parcel in) {
        this.title = in.readString();
        this.album = in.readString();
        this.artist = in.readString();
        this.duration = in.readLong();
        this.albumArtUri = in.readParcelable(Uri.class.getClassLoader());
        this.fileUri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<MusicItem> CREATOR = new Creator<MusicItem>() {
        public MusicItem createFromParcel(Parcel source) {
            return new MusicItem(source);
        }

        public MusicItem[] newArray(int size) {
            return new MusicItem[size];
        }
    };
}
