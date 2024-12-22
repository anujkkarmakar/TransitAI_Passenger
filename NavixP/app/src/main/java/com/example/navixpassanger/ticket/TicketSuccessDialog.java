package com.example.navixpassanger.ticket;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.navixpassanger.R;
import com.example.navixpassanger.sound.SoundPlayer;

public class TicketSuccessDialog extends Dialog {
    private String pnr;
    private OnTicketViewListener listener;
    private SoundPlayer soundPlayer;

    public interface OnTicketViewListener {
        void onViewTicketClicked();
        void onCloseClicked();
    }

    public TicketSuccessDialog(@NonNull Context context, String pnr, OnTicketViewListener listener) {
        super(context);
        this.pnr = pnr;
        this.listener = listener;
        this.soundPlayer = new SoundPlayer(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_booking_success);

        // Make dialog background transparent
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialize views
        TextView pnrText = findViewById(R.id.pnrText);
        Button viewTicketButton = findViewById(R.id.viewTicketButton);
        Button closeButton = findViewById(R.id.closeButton);
        LottieAnimationView animationView = findViewById(R.id.animationView);

        // Set PNR
        pnrText.setText("PNR: " + pnr);

        // Set click listeners
        viewTicketButton.setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.onViewTicketClicked();
            }
        });

        closeButton.setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.onCloseClicked();
            }
        });

        // Start animation and play sound
        animationView.playAnimation();
        soundPlayer.playSuccessSound();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (soundPlayer != null) {
            soundPlayer.release();
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent dialog dismissal on back press
    }
}