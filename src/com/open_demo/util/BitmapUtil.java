package com.open_demo.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

public class BitmapUtil {
	public static final int IMAGE_MAX_SIZE_LIMIT = 100;
	
	private static final int IMAGE_COMPRESSION_QUALITY = 90;
	private static final int NUMBER_OF_RESIZE_ATTEMPTS = 100;
	private static final int MINIMUM_IMAGE_COMPRESSION_QUALITY = 50;
	//private int mWidth;
	//private int mHeight;
	
	public static String compressImage(String path) {

		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, op);
		
		int outWidth = op.outWidth;
		int outHeight = op.outHeight;
		
		int widthLimit = op.outWidth;
		int heightLimit = op.outHeight;
		
		int byteLimit = IMAGE_MAX_SIZE_LIMIT * 1024;

		float scaleFactor = 1.F;
		while ((outWidth * scaleFactor > widthLimit)
				|| (outHeight * scaleFactor > heightLimit)) {
			scaleFactor *= .75F;
		}
		try {
			ByteArrayOutputStream os = null;
			int attempts = 1;
			int sampleSize = 1;
			BitmapFactory.Options options = new BitmapFactory.Options();
			int quality = IMAGE_COMPRESSION_QUALITY;
			Bitmap b = null;
			do {
				options.inSampleSize = sampleSize;
				try {
					b = BitmapFactory.decodeFile(path, options);
					if (b == null) {
						return null; // Couldn't decode and it wasn't because of
					}
				} catch (OutOfMemoryError e) {
					sampleSize *= 2; // works best as a power of two
					attempts++;
					continue;
				}
			} while (b == null && attempts < NUMBER_OF_RESIZE_ATTEMPTS);

			if (b == null) {
				return null;
			}
			boolean resultTooBig = true;
			attempts = 1; // reset count for second loop
			do {
				try {
					if (options.outWidth > widthLimit
							|| options.outHeight > heightLimit
							|| (os != null && os.size() > byteLimit)) {
						int scaledWidth = (int) (outWidth * scaleFactor);
						int scaledHeight = (int) (outHeight * scaleFactor);

						b = Bitmap.createScaledBitmap(b, scaledWidth,
								scaledHeight, false);
						if (b == null) {
							return null;
						}
					}
					os = new ByteArrayOutputStream();
					b.compress(CompressFormat.JPEG, quality, os);
					int jpgFileSize = os.size();
					if (jpgFileSize > byteLimit) {
						quality = (quality * byteLimit) / jpgFileSize; // watch
						if (quality < MINIMUM_IMAGE_COMPRESSION_QUALITY) {
							quality = MINIMUM_IMAGE_COMPRESSION_QUALITY;
						}
						os = new ByteArrayOutputStream();
						b.compress(CompressFormat.JPEG, quality, os);
					}
				} catch (java.lang.OutOfMemoryError e) {
					// Log.w(TAG,
				}
				scaleFactor *= .75F;
				attempts++;
				resultTooBig = os == null || os.size() > byteLimit;
			} while (resultTooBig && attempts < NUMBER_OF_RESIZE_ATTEMPTS);
			b.recycle(); // done with the bitmap, release the memory
			if(resultTooBig){
				return path;
			}
			FileOutputStream out = null;
			try {
				String p = URIUtil.getAppFIlePath()
						+ System.currentTimeMillis() + ".jpg";
				out = new FileOutputStream(p);
				out.write(os.toByteArray());
				out.close();
				return path;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				if(out != null){
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return path;
			
		} catch (java.lang.OutOfMemoryError e) {
			
			return path;
		}
	}

//	public static String compressImage(String imagePath) {
//
//		Bitmap image = BitmapFactory.decodeFile(imagePath);
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
//		int options = 90;
//		while (baos.toByteArray().length / 1024 > IMAGE_MAX_SIZE_LIMIT) {
//			baos.reset();// 重置baos即清空baos
//			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
//			options -= 10;// 每次都减少10
//		}
//		try {
//			String path = PathUtil.getAppFIlePath()
//					+ System.currentTimeMillis() + ".jpg";
//			FileOutputStream out = new FileOutputStream(path);
//			out.write(baos.toByteArray());
//			out.close();
//			return path;
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return imagePath;
//	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	public static Bitmap getSmallBitmap(String filePath, int w, int h) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, w, h);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(filePath, options);
	}

	public static Bitmap getBitmap(String path) {
		if (path == null) {
			return null;
		}
		File f = new File(path);
		if (f.exists()) {
			return BitmapFactory.decodeFile(f.getAbsolutePath());
		} else {
			return null;
		}

	}

	public static String saveBitmapFile(Bitmap bitmap) {
		if(bitmap==null){
			return null;
		}
		File f = new File(URIUtil.getAppFIlePath());
		if (!f.isDirectory()) {
			f.mkdirs();
		}

		File file = new File(URIUtil.getAppFIlePath()
				+ System.currentTimeMillis() + ".jpg");
		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
			return file.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String check(String path) {
		if (path.endsWith(".jpg") || path.endsWith(".jpeg")
				|| path.endsWith(".JPG") || path.endsWith(".JPEG")) {
			return path;
		} 
		Bitmap bmp=getSmallBitmap(path, 50, 50);
		return saveBitmapFile(bmp);
	}
	public static boolean checkCanSend(String path) {
		if (path.endsWith(".jpg") || path.endsWith(".jpeg")
				|| path.endsWith(".JPG") || path.endsWith(".JPEG")) {
			return true;
		} else {
			return false;
		}
	}
	public static boolean isImage(String fileName) {
		// TODO Auto-generated method stub
		if (fileName.endsWith(".png") || fileName.endsWith(".PNG")
				|| fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
				|| fileName.endsWith(".JPG") || fileName.endsWith(".JPEG")) {
			return true;
		}
		return false;
	}

	public static String toJPG(File absoluteFile) {
		 
		return null;
	}

}
