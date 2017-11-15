package in.goods24.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import in.goods24.R;
import in.goods24.pojo.ProductPOJO;

import static in.goods24.util.ConstantsUtil.HEIGHT;
import static in.goods24.util.ConstantsUtil.WIDTH;

/**
 * Created by Shikhar on 11/16/2017.
 */

public class ProductsGridAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ProductPOJO> prodList=new ArrayList<ProductPOJO>();

    public ProductsGridAdapter(Context context, ArrayList<ProductPOJO> prodList){
        this.context=context;
        this.prodList=prodList;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView;
        if(null==convertView){
            gridView = new View(context);
            gridView = inflater.inflate(R.layout.prod_layout_for_grid, null);
            ImageView prodImg = (ImageView) gridView
                    .findViewById(R.id.prodImageGrid);
            String imgStr = prodList.get(position).getProductImage();
            imgStr = imgStr.substring(imgStr.indexOf(",")+1);
            InputStream isImage = new ByteArrayInputStream(Base64.decode(imgStr.getBytes(),Base64.DEFAULT));
            Bitmap bitmap = BitmapFactory.decodeStream(new  FlushedInputStream(isImage));
            prodImg.setImageBitmap(bitmap);
            prodImg.getLayoutParams().height = HEIGHT;
            prodImg.getLayoutParams().width = WIDTH;
            prodImg.requestLayout();
            TextView prodNameTxt = (TextView)gridView.findViewById(R.id.prodNameGrid);
            TextView prodDescTxt = (TextView)gridView.findViewById(R.id.prodDescGrid);
            TextView prodPriceTxt = (TextView)gridView.findViewById(R.id.prodPriceGrid);
            TextView prodDiscTxt = (TextView)gridView.findViewById(R.id.prodDiscGrid);
            prodNameTxt.setText(prodList.get(position).getProductName());
            prodDescTxt.setText(prodList.get(position).getProductDesc());
            prodPriceTxt.setText(prodList.get(position).getProductRate());
            prodDiscTxt.setText(prodList.get(position).getProductDiscount());
        }
        else {
            gridView = (View) convertView;
        }

        return gridView;
    }
    @Override
    public int getCount() {
        return prodList.size();
    }
    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
}
