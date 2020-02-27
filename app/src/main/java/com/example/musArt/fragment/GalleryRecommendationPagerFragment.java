package com.example.musArt.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.musArt.R;
import com.example.musArt.model.AlbumModel;
import com.example.musArt.view.utils.UtilsUI;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GalleryRecommendationPagerFragment extends DialogFragment {

    @BindView(R.id.headerLayoutOfRecommendationGallery)
    RelativeLayout headerLayoutOfRecommendationGallery;

    @BindView(R.id.backFromRecommendationGallery)
    ImageView backFromRecommendationGallery;

    @BindView(R.id.nameOfRecommendationAlbum)
    TextView nameOfRecommendationAlbum;

    @BindView(R.id.nameOfRecommendationGroup)
    TextView nameOfRecommendationGroup;

    @BindView(R.id.moreOfRecommendationCover)
    ImageView moreOfRecommendationCover;

    @BindView(R.id.coverRecommendationPager)
    ViewPager coverRecommendationPager;

    private View galleryView;

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(STYLE_NORMAL,
                android.R.style.Theme_Black_NoTitleBar);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommendation_gallery_cover,
                container, false);
        ButterKnife.bind(this, view);
        context = getContext();
        galleryView = view;
        showSystemUI();
        backFromRecommendationGallery.setOnClickListener(v -> dismiss());
        Bundle bundle = getArguments();
        assert bundle != null;
        AlbumModel[] albumModels = (AlbumModel[]) bundle.get("albumModelList");
        if (albumModels != null) {
            final List<AlbumModel> albumModelList = Arrays.asList(albumModels);
            final GalleryRecommendationPagerFragment.PhotoAdapter photoAdapter =
                    new GalleryRecommendationPagerFragment.PhotoAdapter(albumModelList);
            int artPosition = (int) bundle.get("albumPositionId");
            coverRecommendationPager.setAdapter(photoAdapter);
            coverRecommendationPager.setCurrentItem(artPosition);
            photoAdapter.notifyDataSetChanged();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Objects.requireNonNull(dialog.getWindow())
                    .setWindowAnimations(R.style.dialog_animation_fade);
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void hideSystemUI() {
        galleryView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void showSystemUI() {
        galleryView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    class PhotoAdapter extends PagerAdapter implements PopupMenu.OnMenuItemClickListener {

        private final List<AlbumModel> albumModels;

        private AlbumModel selectedAlbumModel;

        PhotoAdapter(List<AlbumModel> albumModelArts) {
            this.albumModels = albumModelArts;
        }

        @Override
        public int getCount() {
            return albumModels.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @NonNull
        @SuppressLint({"ResourceType", "ClickableViewAccessibility"})
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView artView = new PhotoView(context);
            artView.setZoomable(true);
            artView.setMinimumScale(1.0f);
            artView.setMaximumScale(2.0f);
            final AlbumModel albumModel = albumModels.get(position);
            artView.setImageBitmap(albumModel.getArt());

            artView.setOnPhotoTapListener((view, x, y) -> {
                if (headerLayoutOfRecommendationGallery.getVisibility() == View.VISIBLE) {
                    headerLayoutOfRecommendationGallery.setVisibility(View.INVISIBLE);
                    hideSystemUI();
                } else {
                    headerLayoutOfRecommendationGallery.setVisibility(View.VISIBLE);
                    showSystemUI();
                }
            });

            moreOfRecommendationCover.setOnClickListener(v -> {
                PopupMenu popupMenuArt = new PopupMenu(context, v);
                popupMenuArt.setOnMenuItemClickListener(this);
                popupMenuArt.inflate(R.menu.menu_gallery_art);
                popupMenuArt.show();
            });

            coverRecommendationPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {
                    selectedAlbumModel = albumModels.get(position);
                    nameOfRecommendationAlbum.setText(selectedAlbumModel.getName());
                    nameOfRecommendationGroup.setText(selectedAlbumModel.getGroupName());
                }

                @Override
                public void onPageSelected(int position) { }

                @Override
                public void onPageScrollStateChanged(int state) { }
            });
            container.addView(artView, 0);
            return artView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView) object);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.share_art_of_gallery) {
                UtilsUI utilsUI = new UtilsUI(context);
                utilsUI.shareOfCover(selectedAlbumModel.getArt());
            }
            return false;
        }

    }

}
