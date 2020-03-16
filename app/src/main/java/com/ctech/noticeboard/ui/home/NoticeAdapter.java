package com.ctech.noticeboard.ui.home;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.ctech.noticeboard.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.MyViewHolder> {
    private List<HomeViewModel> noticeList;

    private Dialog mDialog;


    @NonNull
    @Override
    public NoticeAdapter.MyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notice_list, parent, false);
        final MyViewHolder myViewHolder = new MyViewHolder(itemView);

        mDialog = new Dialog(parent.getContext());
        mDialog.setContentView(R.layout.dialog_notice_view);
        mDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                myViewHolder.item_notice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HomeViewModel notice = noticeList.get(myViewHolder.getAdapterPosition());

                TextView dialogTitle = (TextView) mDialog.findViewById(R.id.txt_dialog_title);
                TextView dialogGenre = (TextView) mDialog.findViewById(R.id.txt_dialog_genre);
                TextView dialogDate = (TextView) mDialog.findViewById(R.id.txt_dialog_date);
                TextView dialogDesc = (TextView) mDialog.findViewById(R.id.txt_dialog_desc);
                ImageView dialogImage = (ImageView) mDialog.findViewById(R.id.img_dialog_image);

                dialogTitle.setText(notice.getTitle());
                dialogGenre.setText(notice.getGenre());
                dialogDate.setText(notice.getDate());
                dialogDesc.setText(notice.getDescription());
                Picasso.get().load(notice.getImageUrl()).into(dialogImage);

                mDialog.show();
               }
        });

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeAdapter.MyViewHolder holder, int position) {
        HomeViewModel notice = noticeList.get(position);
        holder.title.setText(notice.getTitle());
        holder.genre.setText(notice.getGenre());
        holder.date.setText(notice.getDate());
        Picasso.get().load(notice.getImageUrl()).centerCrop(Gravity.TOP).fit().into(holder.img);
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, genre;
        ImageView img;
        RelativeLayout item_notice;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            genre = view.findViewById(R.id.genre);
            date = view.findViewById(R.id.date);
            img = view.findViewById(R.id.img_preview);
            item_notice = view.findViewById(R.id.item_notice_id);
        }
    }

    public NoticeAdapter(List<HomeViewModel> noticeList) {
        this.noticeList = noticeList;
    }
}
