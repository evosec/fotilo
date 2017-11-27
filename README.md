# fotilo

fotilo is an Android Camera App. It can be called via an Intent and will return all the photos taken in the result. This is similar to `MediaStore.ACTION_IMAGE_CAPTURE` but for multiple photos. It additionally allows configuring some camera options from the calling App like resolution and aspect ratio.

## Configuration

You can configure the photos taken by this app by passing parameters in an `Intent`.

```java
Intent intent = new Intent("de.evosec.fotilo");
```

### Configure resolution

Put your resolution in the intent. fotilo will automatically select the correct native resolution that has the same aspect ratio. If the camera does not natively support your resolution the next highest available with the same aspect ratio will be selected.

```java
intent.putExtra("width", 1920);
intent.putExtra("height", 1080);
```

### Configure aspect ratio

You can also pass an aspect ratio:

```java
intent.putExtra("aspectratio", (double) width / (double) height);
```

### Limit number of photos

To limit the number of photos to take, put the following in your intent:

```java
intent.putExtra("maxPictures", 2);
```

## Get the photos

After the photos are taken, you can access them from the activityResult in your own Activity:

```java
public void getImagesFromActivityResult(Intent data) {
    Bundle bundle = data.getBundleExtra("data");
    try {
        List<String> photos = bundle.getStringArrayList("pictures");
        String error = bundle.getString("error");
        if(error == null && photos != null) {
            fillImageAdapter();
        } else if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        }
    } catch (Exception ex) {
        Log.d(TAG, ex.getMessage());
    }
}
```
