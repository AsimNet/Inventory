package net.asim.inventory;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import net.asim.inventory.Data.InventoryContract;

/**
 * Created by asimaltwijry on 5/5/17.
 */


public class InventoryCursorAdapter extends CursorAdapter {

    private static final String TAG = InventoryCursorAdapter.class.getSimpleName();


    protected InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView product_name = (TextView) view.findViewById(R.id.product_name);
        TextView product_quantity = (TextView) view.findViewById(R.id.available_in_stock);
        TextView product_price = (TextView) view.findViewById(R.id.product_price);
        TextView product_sold = (TextView) view.findViewById(R.id.sold);
        Button product_add_btn = (Button) view.findViewById(R.id.plus_button);
        ImageView product_thumbnail = (ImageView) view.findViewById(R.id.proudct_image);


        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COL_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COL_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COL_PRICE);
        int thumbnailColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COL_PICTURE);
        int salesColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COL_ITEMS_SOLD);

        int id = cursor.getInt(cursor.getColumnIndex(InventoryContract.ProductEntry._ID));
        final String productName = cursor.getString(nameColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        final int products_sold = cursor.getInt(salesColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        Uri thumbUri = Uri.parse(cursor.getString(thumbnailColumnIndex));

        String productQuantity = String.valueOf(quantity);
        String productSold = String.valueOf(products_sold);

        final Uri currentProductUri = ContentUris.withAppendedId(InventoryContract.ProductEntry.CONTENT_URI, id);

        Log.d(TAG, "genero Uri: " + currentProductUri + " Product name: " + productName + " id: " + id);

        product_name.setText(productName);
        product_quantity.setText(productQuantity);
        product_price.setText(productPrice);
        product_sold.setText(productSold);
        //We use Glide to import photo images
        Glide.with(context).load(thumbUri)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.drawable.ic_insert_photo)
                .crossFade()
                .centerCrop()
                .into(product_thumbnail);


        product_add_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (quantity > 0) {
                    int qq = quantity;
                    int yy = products_sold;
                    values.put(InventoryContract.ProductEntry.COL_QUANTITY, --qq);
                    values.put(InventoryContract.ProductEntry.COL_ITEMS_SOLD, ++yy);
                    resolver.update(
                            currentProductUri,
                            values,
                            null,
                            null
                    );
                    context.getContentResolver().notifyChange(currentProductUri, null);
                } else {
                    Toast.makeText(context, R.string.item_out_of_stock, Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
