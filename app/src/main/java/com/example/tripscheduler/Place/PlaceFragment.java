package com.example.tripscheduler.Place;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.tripscheduler.R;
import com.example.tripscheduler.Server.BitmapArithmetic;
import com.example.tripscheduler.Server.IAppService;
import com.example.tripscheduler.Server.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PlaceFragment extends Fragment {

  IAppService apiService;
  private final String SERVER = "http://143.248.36.205:3000";
  private CompositeDisposable compositeDisposable = new CompositeDisposable();
  private IAppService iAppService;
  PlaceAdapter adapter;

  RecyclerView mRecyclerView;
  ArrayList<TPlace> TPlaceList;

  String title;
  String email;

  Button uploadButton;

  Context context = getActivity();

  public PlaceFragment(String title, String email) {

    this.title = title;
    this.email = email;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.layout_fragmentplace, container, false);

    uploadButton = (Button) rootView.findViewById(R.id.upload_image);

    Retrofit retrofitClient = RetrofitClient.getInstance();
    iAppService = retrofitClient.create(IAppService.class);

    initRetrofitClient();

    TPlaceList = new ArrayList<>();

    mRecyclerView = (RecyclerView) rootView.findViewById(R.id.masonryGrid);
    mRecyclerView
        .setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

    adapter = new PlaceAdapter(getActivity(), TPlaceList);

    mRecyclerView.addOnItemTouchListener(new OnItemTouchListener() {
      @Override
      public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        return false;
      }

      @Override
      public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        int position = rv.getChildAdapterPosition(child);
        TPlace place = TPlaceList.get(position);

        Intent intent = new Intent(context,PlaceInfoActivity.class);

      }

      @Override
      public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

      }
    });

    mRecyclerView.setAdapter(adapter);
    PlaceItemDecoration decoration = new PlaceItemDecoration(16);
    mRecyclerView.addItemDecoration(decoration);

    compositeDisposable.add(iAppService.places_get_one(email, title)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .retry()
        .subscribe(new Consumer<String>() {
          @Override
          public void accept(String data) throws Exception {
            Log.e("places_get_one", data);

            if (data.equals("0")) {
              System.out.println("No data existed");
            } else {
              JsonParser jsonParser = new JsonParser();
              JsonArray jsonArray = (JsonArray) jsonParser.parse(data);

              for (int i = 0; i < jsonArray.size(); i++) {
                final JsonObject object = (JsonObject) jsonArray.get(i);
                System.out.println(object.get("name"));
                System.out.println(object.get("location"));
                System.out.println(object.get("label"));
                System.out.println(object.get("path"));

                JsonArray location = (JsonArray) object.get("location");
                System.out.println(location.get(0));
                System.out.println(location.get(1));

                System.out.println(SERVER + "/" + object.get("path").toString().replace("\"", ""));

                String url = SERVER + "/" + object.get("path").toString().replace("\"", "");

                TPlaceList.add(
                    new TPlace(object.get("name").toString(), object.get("location").toString(),
                        object.get("label").toString(), url));
                adapter.notifyDataSetChanged();

              }
            }
          }
        }));

    return rootView;
  }

  private void initRetrofitClient() {
    OkHttpClient client = new OkHttpClient.Builder().build();
    apiService = new Retrofit.Builder()
        .baseUrl(SERVER + "/")
        .client(client)
        .build()
        .create(IAppService.class);
  }

  private void multipartImageUpload(Bitmap mBitmap, String email, String title, final String name,
      final String location, final String label) {
    try {
      File filesDir = getContext().getFilesDir();
      File file = new File(filesDir, "image" + ".png");

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      mBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
      byte[] bitmapData = bos.toByteArray();

      FileOutputStream fos = new FileOutputStream(file);
      fos.write(bitmapData);
      fos.flush();
      fos.close();

      RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));
      MultipartBody.Part body = MultipartBody.Part
          .createFormData("upload", file.getName(), reqFile);
      RequestBody imageName = RequestBody.create("upload", MediaType.parse("text/plain"));
      RequestBody send_email = RequestBody.create(email, MediaType.parse("text/plain"));
      RequestBody send_title = RequestBody.create(title, MediaType.parse("text/plain"));
      RequestBody send_name = RequestBody.create(name, MediaType.parse("text/plain"));
      RequestBody send_location = RequestBody.create(location, MediaType.parse("text/plain"));
      RequestBody send_label = RequestBody.create(label, MediaType.parse("text/plain"));

      Call<ResponseBody> req = apiService
          .place_insert_one(body, imageName, send_email, send_title, send_name, send_location,
              send_label);
      req.enqueue(new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
          if (response.code() == 200) {
            Toast.makeText(getContext(), "Upload Success!", Toast.LENGTH_SHORT).show();

            System.out.println(response.toString());
            System.out.println(name);
            System.out.println(location);
            System.out.println(label);

            TPlaceList.add(new TPlace(name, location, label, response.toString()));
            adapter.notifyDataSetChanged();
          } else {
            Toast.makeText(getContext(), "Error : " + response.code(), Toast.LENGTH_SHORT).show();
          }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
          Toast.makeText(getContext(), "Request failed.", Toast.LENGTH_SHORT).show();
          t.printStackTrace();
        }
      });
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void addPlace(String name, String strLatLng, String label, Bitmap image) {

    multipartImageUpload(image, title, email.replace("\"", ""), name, strLatLng, label);
  }


}
