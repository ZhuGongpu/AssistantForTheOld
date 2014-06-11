package view.call;

import android.app.Activity;
import android.content.*;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.*;
import android.widget.*;
import tts.TTS;
import view.main.MainActivity;
import view.main.R;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class ContactActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    private ArrayList<View> viewArrayList = new ArrayList<View>();
    private ArrayList<String> nameList = new ArrayList<String>();
    private ArrayList<Bitmap> bitmapList = new ArrayList<Bitmap>();
    private ArrayList<String> phoneNumber = new ArrayList<String>();
    private HashMap<String, String> phoneNumberToId = new HashMap<String, String>();
    private int currentIndex;
    private ViewPager myPager;
    private Gallery myGallery;
    private MyPagerAdapter myPagerAdapter;
    private MyGalleryAdapter myGalleryAdapter;

    private String currentName = null;

    private int currentPager = -1;


    private TTS tts = null;

    private static byte[] bitmapToBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 将Bitmap压缩成PNG编码，质量为100%存储
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);//除了PNG还有很多常见格式，如jpeg等。
        return os.toByteArray();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_activity_contact_layout);

        tts = new TTS(this);
        tts.init();

        myPager = (ViewPager) findViewById(R.id.contact_viewpager);
        myGallery = (Gallery) findViewById(R.id.contact_gallery);
        resolve();
        initPagerItems();
        myPagerAdapter = new MyPagerAdapter();
        myGalleryAdapter = new MyGalleryAdapter(this);
        myPager.setAdapter(myPagerAdapter);
        myGallery.setAdapter(myGalleryAdapter);

        myGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                myPagerAdapter.notifyDataSetChanged();
                myPager.setCurrentItem(position % bitmapList.size());

                currentIndex = position % bitmapList.size();
            }
        });

        myGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                myPagerAdapter.notifyDataSetChanged();
                myPager.setCurrentItem(position % bitmapList.size());
                currentIndex = position % bitmapList.size();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        myPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                if (i != currentPager) {

                    Log.v("pager", i + "  ");

                    //TODO: tts
                    String contentToRead = nameList.get(i);
                    tts.getsSpeechSynthesizer().startSpeaking(contentToRead, null);

                    currentPager = i;
                }
            }

            @Override
            public void onPageSelected(int i) {

                currentIndex = i;
                myGallery.setSelection(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contactmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.contactMenu:

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 1);
                break;

        }
        return super.onOptionsItemSelected(item);

    }

    private void initPagerItems() {
        for (int i = 0; i < bitmapList.size(); i++) {
            View v = LayoutInflater.from(this).inflate(R.layout.call_activity_itempage, null);
            ImageView imageView = (ImageView) v.findViewById(R.id.contactIv);
            TextView textView = (TextView) v.findViewById(R.id.contactTv);
            imageView.setImageBitmap(bitmapList.get(i));
            textView.setText(nameList.get(i));
            viewArrayList.add(v);

        }
    }

    public void resolve() {
        ContentResolver resolver = getContentResolver();
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        Uri dataUri = Uri.parse("content://com.android.contacts/data");
        Cursor cursor = resolver.query(uri, null, null, null, null);


        while (cursor.moveToNext()) {

            String id = cursor.getString(cursor.getColumnIndex("contact_id"));
            Cursor cursor1 = resolver.query(dataUri, null, "raw_contact_id=?", new String[]{id}, null);

            while (cursor1.moveToNext()) {
                String data = cursor1.getString(cursor1.getColumnIndex("data1"));
                String mimetype = cursor1.getString(cursor1.getColumnIndex("mimetype"));
                String name = cursor1.getString(cursor1.getColumnIndex("display_name"));

                if (mimetype.equals("vnd.android.cursor.item/phone_v2")) {
                    data = data.replace(" ", "");
                    phoneNumber.add(data);
                    getImage(data);
                    phoneNumberToId.put(data, id);
                    nameList.add(name);
                }

            }

            cursor1.close();


        }
        cursor.close();


    }

    public void getImage(String strPhoneNumber) {
        // 取得Intent中的頭像

        //通话电话号码获取头像uri
        Uri uriNumber2Contacts = Uri
                .parse("content://com.android.contacts/"
                        + "data/phones/filter/" + strPhoneNumber);
        Cursor cursorCantacts = ContactActivity.this.getContentResolver().query(uriNumber2Contacts, null, null,
                null, null);
        if (cursorCantacts.getCount() > 0) { //若游标不为0则说明有头像,游标指向第一条记录
            cursorCantacts.moveToFirst();
            Long contactID = cursorCantacts.getLong(cursorCantacts
                    .getColumnIndex("contact_id"));
            Uri uri = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI, contactID);
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                    ContactActivity.this.getContentResolver(), uri
            );
            if (input == null) {
                Bitmap defaultbitmap = BitmapFactory.decodeResource(ContactActivity.this.getResources(), R.drawable.defaultcontact);
                bitmapList.add(defaultbitmap);
            } else {
                Bitmap btContactImage = BitmapFactory.decodeStream(input);
                bitmapList.add(btContactImage);
            }


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            String sdStatus = Environment.getExternalStorageState();
            if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
                Log.i("TestFile",
                        "SD card is not avaiable/writeable right now.");
                return;
            }
            String name = new DateFormat().format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
            Toast.makeText(this, name, Toast.LENGTH_LONG).show();
            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式

            FileOutputStream b = null;
            File file = new File("/sdcard/myImage/");
            if (!file.exists())
                file.mkdirs();// 创建文件夹

            String fileName = "/sdcard/myImage/" + name;
            File photofile = new File(fileName);
            if (!photofile.exists()) {
                try {
                    photofile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                b = new FileOutputStream(photofile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    b.flush();
                    b.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //动态的改变gridview中的一个Item的内容
            bitmapList.set(currentIndex, bitmap);
            myGalleryAdapter.notifyDataSetChanged();
            updatePhoto(Long.valueOf(phoneNumberToId.get(phoneNumber.get(currentIndex))), bitmapToBytes(bitmap));
//            ((ImageView) findViewById(R.id.imageView)).setImageBitmap(bitmap);// 将图片显示在ImageView里
            Intent intent = new Intent(ContactActivity.this, ContactActivity.class);
            this.startActivity(intent);
            this.finish();
        }
    }

    private void updatePhoto(Long rawContactId, byte[] photo) {

        ContentValues values = new ContentValues();
        values.put(ContactsContract.Contacts.Photo.PHOTO, photo);

        String selection = ContactsContract.RawContacts.Data.RAW_CONTACT_ID + "=? and " + ContactsContract.RawContacts.Data.MIMETYPE
                + "=?";
        String[] selectionArgs = new String[]{
                Long.toString(rawContactId), ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE};

        getContentResolver().update(ContactsContract.Data.CONTENT_URI, values, selection,
                selectionArgs);


    }

    class MyPagerAdapter extends PagerAdapter {
        private int mChildCount = 0;

        @Override
        public int getCount() {

            return viewArrayList.size();
        }

        @Override
        public void destroyItem(View container, int position, Object object) {

            ((ViewPager) container).removeView(viewArrayList.get(position));
        }


        @Override
        public Object instantiateItem(final View container, final int position) {

            ((ViewPager) container).addView(viewArrayList.get(position));
            viewArrayList.get(position).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //read current page
                    String currentSelectedItem = "将与" + nameList.get(position) + "通话";
                    tts.getsSpeechSynthesizer().startSpeaking(currentSelectedItem, null);


                    //jump to call activity
                    Intent intent = new Intent();
                    intent.putExtra("name", nameList.get(position));
                    intent.putExtra("number", phoneNumber.get(position));
                    intent.setClass(ContactActivity.this, CallActivity.class);

                    startActivity(intent);
                    finish();

                }
            });
            return viewArrayList.get(position);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {

            return arg0 == arg1;
        }

        @Override
        public int getItemPosition(Object object) {
            if (mChildCount > 0) {
                mChildCount--;
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }
    }

    class MyGalleryAdapter extends BaseAdapter {

        Context context;
        int itemBacklground;

        public MyGalleryAdapter(Context context) {
            this.context = context;
            TypedArray array = context.obtainStyledAttributes(R.styleable.gallery);
            itemBacklground = array.getResourceId(R.styleable.gallery_android_galleryItemBackground, 0);
            array.recycle();
        }

        public int getCount() {

            return Integer.MAX_VALUE;

        }

        public Object getItem(int position) {

            return null;
        }

        public long getItemId(int position)

        {

            return position;

        }


        public View getView(int position, View convertView, ViewGroup parent)

        {

            ImageView view;

            view = (ImageView) convertView;

            if (view == null) {

                view = new ImageView(context);

            }


            view.setImageBitmap(bitmapList.get(position % bitmapList.size()));
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);

            view.setLayoutParams(new Gallery.LayoutParams(250, 200));

            view.setBackgroundColor(Color.LTGRAY);
            view.setBackgroundResource(itemBacklground);

            return view;

        }
    }

}


