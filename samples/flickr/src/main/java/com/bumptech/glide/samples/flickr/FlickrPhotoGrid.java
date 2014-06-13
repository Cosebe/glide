package com.bumptech.glide.samples.flickr;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.actionbarsherlock.app.SherlockFragment;
import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.samples.flickr.api.Photo;

import java.util.ArrayList;
import java.util.List;

public class FlickrPhotoGrid extends SherlockFragment implements PhotoViewer {
    private static final String IMAGE_SIZE_KEY = "image_size";
    private static final String PRELOAD_KEY = "preload";

    private PhotoAdapter adapter;
    private List<Photo> currentPhotos;
    private int photoSize;

    public static FlickrPhotoGrid newInstance(int size, int preloadCount) {
        FlickrPhotoGrid photoGrid = new FlickrPhotoGrid();
        Bundle args = new Bundle();
        args.putInt(IMAGE_SIZE_KEY, size);
        args.putInt(PRELOAD_KEY, preloadCount);
        photoGrid.setArguments(args);
        return photoGrid;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        photoSize = args.getInt(IMAGE_SIZE_KEY);

        final View result = inflater.inflate(R.layout.flickr_photo_grid, container, false);
        final GridView grid = (GridView) result.findViewById(R.id.images);
        grid.setColumnWidth(photoSize);
        final FlickrPreloader preloader = new FlickrPreloader(getActivity(), args.getInt(PRELOAD_KEY));
        grid.setOnScrollListener(preloader);
        adapter = new PhotoAdapter();
        grid.setAdapter(adapter);
        if (currentPhotos != null)
            adapter.setPhotos(currentPhotos);

        return result;
    }

    @Override
    public void onPhotosUpdated(List<Photo> photos) {
        currentPhotos = photos;
        if (adapter != null)
            adapter.setPhotos(currentPhotos);
    }

    private class FlickrPreloader extends ListPreloader<Photo> {
        private final int[] dimens = new int[] { photoSize, photoSize };
        private final Context context;

        public FlickrPreloader(Context context, int toPreload) {
            super(toPreload);
            this.context = context;
        }

        @Override
        protected int[] getDimensions(Photo item) {
            return dimens;
        }

        @Override
        protected List<Photo> getItems(int start, int end) {
            return currentPhotos.subList(start, end);
        }

        @Override
        protected GenericRequestBuilder getRequestBuilder(Photo item) {
            return Glide.with(context)
                    .loadFromImage(item)
                    .centerCrop();
        }
    }

    private class PhotoAdapter extends BaseAdapter {
        private List<Photo> photos = new ArrayList<Photo>(0);
        private final LayoutInflater inflater;

        public PhotoAdapter() {
            this.inflater = LayoutInflater.from(getActivity());
        }

        public void setPhotos(List<Photo> photos) {
            this.photos = photos;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return photos.size();
        }

        @Override
        public Object getItem(int i) {
            return photos.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup container) {
            final Photo current = photos.get(position);
            final ImageView imageView;
            if (view == null) {
                imageView = (ImageView) inflater.inflate(R.layout.flickr_photo_grid_item, container, false);
                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                params.width = photoSize;
                params.height = photoSize;
            } else {
                imageView = (ImageView) view;
            }

            Glide.with(getActivity())
                    .loadFromImage(current)
                    .animate(R.anim.fade_in)
                    .centerCrop()
                    .into(imageView);

            return imageView;
        }

    }
}