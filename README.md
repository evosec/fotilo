# fotilo
Android app to take pictures to work together with our EvoCam.

##Configuration
You can configure this app by passing paramters in an intent.
```java
  Intent intent = new Intent("de.evosec.fotilo");
```
###Configure min. picture resolution
Put your min. picture resolution in the intent, for example:
```java
  intent.putExtra("width", 1920);
  intent.putExtra("height", 1080);
```
###Configure aspectratio
Insteat of a picture resolution, you can put an aspectratio to the intent, too:
```java
  intent.putExtra("aspectratio", (double) width / (double) height);
```
###Configure max. number of taking pictures
To allow only a specific number of pictures to take, put the following in your intent:
```java
  intent.putExtra("maxPictures", 2);
```
##Get the pictures
After the pictures are taken, you can access them from the activityResult in your own Activity:
```java
  public void getImagesFromActivityResult(Intent data) {
        Bundle bundle = data.getBundleExtra("data");
        String error = "";
        try {
            pictures = bundle.getStringArrayList("pictures");
            error = bundle.getString("error");
            if(error == null && pictures != null) {
                fillImageAdapter();
            } else if (error != null){
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
    }
```
