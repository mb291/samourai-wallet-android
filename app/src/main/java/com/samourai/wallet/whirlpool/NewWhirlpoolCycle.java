package com.samourai.wallet.whirlpool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.samourai.wallet.R;
import com.samourai.wallet.api.APIFactory;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.whirlpool.adapters.CoinsAdapter;
import com.samourai.wallet.whirlpool.models.Coin;

import java.util.ArrayList;
import java.util.List;

public class NewWhirlpoolCycle extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CoinsAdapter coinsAdapter;
    private List<Coin> coins = new ArrayList<Coin>();
    private List<MyTransactionOutPoint> outPoints = new ArrayList<MyTransactionOutPoint>();
    private ViewGroup reviewButton;
    private TextView tvTotalSelected = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_whirlpool_cycle);
        Toolbar toolbar = findViewById(R.id.toolbar_new_whirlpool);
        recyclerView = findViewById(R.id.coins_recyclerview);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvTotalSelected = findViewById(R.id.totalSelected);
        reviewButton = findViewById(R.id.review_button);
        coinsAdapter = new CoinsAdapter(this, coins, tvTotalSelected);
        loadUTXOs();
        recyclerView.setAdapter(coinsAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new SeparatorDecoration(this, ContextCompat.getColor(this, R.color.item_separator_grey), 1));

        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                long totalSelected = 0L;
                for(Coin coin : coins)   {
                    if(coin.getSelected())  {
                        totalSelected += coin.getValue();
                    }
                }

                Toast.makeText(NewWhirlpoolCycle.this,"Selected: ".concat(getSelectedCoins().toString() + "\n" + org.bitcoinj.core.Coin.valueOf(totalSelected).toPlainString().concat(" BTC")),Toast.LENGTH_LONG).show();
                Intent intent = new Intent(NewWhirlpoolCycle.this,WhirlPoolActivity.class);
                startActivity(intent);
            }
        });

        tvTotalSelected.setText(getText(R.string.total_selected) + ": " + "0 BTC");
    }

    private ArrayList<Coin> getSelectedCoins() {
        ArrayList<Coin> coins = new ArrayList<Coin>();
        List<Coin> adapterCoins = coinsAdapter.getCoins();
        for (int i = 0; i < coinsAdapter.getCoins().size(); i++) {
            if (adapterCoins.get(i).getSelected()) {
                coins.add(adapterCoins.get(i));
            }
        }
        return coins;
    }

    private void loadUTXOs() {

        List<UTXO> utxos = APIFactory.getInstance(NewWhirlpoolCycle.this).getUtxos(true);
        outPoints.clear();
        for(UTXO utxo : utxos)  {
            outPoints.addAll(utxo.getOutpoints());
        }
        for(MyTransactionOutPoint outPoint : outPoints)   {
            Coin coin = new Coin();
            coin.setAddress(outPoint.getAddress());
            coin.setValue(outPoint.getValue().longValue());
            coins.add(coin);
        }
    }

    // RV decorator that sets custom divider for the list
    private class SeparatorDecoration extends RecyclerView.ItemDecoration {

        private final Paint mPaint;

        SeparatorDecoration(@NonNull Context context, @ColorInt int color,
                            @FloatRange(from = 0, fromInclusive = false) float heightDp) {
            mPaint = new Paint();
            mPaint.setColor(color);
            final float thickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    heightDp, context.getResources().getDisplayMetrics());
            mPaint.setStrokeWidth(thickness);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();

            final int position = params.getViewAdapterPosition();

            if (position < state.getItemCount()) {
                outRect.set(0, 0, 0, (int) mPaint.getStrokeWidth()); // left, top, right, bottom
            } else {
                outRect.setEmpty(); // 0, 0, 0, 0
            }
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            final int offset = (int) (mPaint.getStrokeWidth() / 2);
            for (int i = 0; i < parent.getChildCount(); i++) {
                // get the view
                final View view = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();

                // get the position
                final int position = params.getViewAdapterPosition();
                // draw top separator
                c.drawLine(view.getLeft(), view.getTop() + offset, view.getRight(), view.getTop() + offset, mPaint);

                if (position == state.getItemCount() - 1) {
                    // draw bottom line for the last one
                    c.drawLine(view.getLeft(), view.getBottom() + offset, view.getRight(), view.getBottom() + offset, mPaint);
                }
            }
        }
    }

}
