package com.open_demo.util;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.widget.ImageView;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeGroup;
import com.gotye.api.GotyeUser;
import com.open_demo.R;

public class ImageCache {
	
	private ImageCache() {
		// use 1/8 of available heap size
		cache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 8)) {
              @Override
              protected int sizeOf(String key, Bitmap value) {
                  return value.getRowBytes() * value.getHeight();
              }
          };
	}

	private static Map<String, Boolean> hasDownload=new  HashMap<String, Boolean>(); 
	private static ImageCache imageCache = null;

	public static synchronized ImageCache getInstance() {
		if (imageCache == null) {
			imageCache = new ImageCache();
		}
		return imageCache;

	}
	private LruCache<String, Bitmap> cache = null;
	
	
	public void setIcom(ImageView iconView,GotyeUser user){
		 Bitmap bmp=ImageCache.getInstance().get(user.getName());
   	  if(bmp!=null){
   		  iconView.setImageBitmap(bmp);
   	  }else{
   		  bmp=BitmapUtil.getBitmap(user.getIcon().getPath());
   		  if(bmp!=null){
   			iconView.setImageBitmap(bmp);
   			put(user.getName(), bmp);
   		  }else{
   			  
   			  iconView.setImageResource(R.drawable.head_icon_user);
   			  if(user.getIcon().getUrl()==null){
   				  return;
   			  }
   			  if(!hasDownload.containsKey(user.getIcon().getUrl())){
   				hasDownload.put(user.getIcon().getUrl(), true);
   				 GotyeAPI.getInstance().downloadMedia(user.getIcon());
   			  }
   			 
   		  }
   	  }
   	
	}
	public void setIcom(ImageView iconView,GotyeGroup group){
		Bitmap bmp=ImageCache.getInstance().get(group.getId()+"");
		if(bmp!=null){
			iconView.setImageBitmap(bmp);
		}else{
			bmp=BitmapUtil.getBitmap(group.getIcon().getPath());
			if(bmp!=null){
				iconView.setImageBitmap(bmp);
				put(group.getId()+"", bmp);
			}else{
				
				iconView.setImageResource(R.drawable.head_icon_user);
				if(group.getIcon().getUrl()==null){
					return;
				}
				if(!hasDownload.containsKey(group.getIcon().getUrl())){
					hasDownload.put(group.getIcon().getUrl(), true);
					GotyeAPI.getInstance().downloadMedia(group.getIcon());
				}
				
			}
		}
		
	}
	
	/**
	 * put bitmap to image cache
	 * @param key
	 * @param value
	 * @return  the puts bitmap
	 */
	public Bitmap put(String key, Bitmap value){
		if(TextUtils.isEmpty(key)){
			return null;
		}
		if(value==null){
			return null;
		}
		return cache.put(key, value);
	}
	
	public void removeKey(String key){
		if(cache!=null){
			cache.remove(key);
		}
	}
	
	/**
	 * return the bitmap
	 * @param key
	 * @return
	 */
	public Bitmap get(String key){
		if(key==null){
			return null;
		}
		return cache.get(key);
	}
	 
	public void clear(){
		cache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 8)) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
	}
}
