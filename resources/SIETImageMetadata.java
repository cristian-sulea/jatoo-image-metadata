/*
 * Copyright (C) Cristian Sulea ( http://cristian.sulea.net )
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jatoo.image.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

/**
 * {@link SIETImageMetadata}
 * 
 * @version 1.0.0, November 27, 2011
 * @author Cristian Sulea ( http://cristiansulea.entrust.ro )
 */
public class SIETImageMetadata {

  private String fileName;
  private long fileSize;

  private Date date;

  private int width;
  private int height;

  private String camera;
  private String model;
  private String software;

  private int ISO;

  private String exposure;

  private String aperture;

  private String focalLength;

  public static SIETImageMetadata getMetadata(File file) throws Exception {

    SIETImageMetadata sietMetadata = new SIETImageMetadata();

    sietMetadata.fileName = file.getName();
    sietMetadata.fileSize = file.length();

    JpegImageMetadata jpegMetadata = (JpegImageMetadata) Sanselan.getMetadata(file);

    sietMetadata.date = getFieldAsDate(jpegMetadata, TiffConstants.EXIF_TAG_CREATE_DATE, TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL, TiffConstants.EXIF_TAG_MODIFY_DATE);

    sietMetadata.width = getFieldAsInt(jpegMetadata, TiffConstants.EXIF_TAG_IMAGE_WIDTH, TiffConstants.EXIF_TAG_EXIF_IMAGE_WIDTH, TiffConstants.EXIF_TAG_IMAGE_WIDTH_IFD0);
    sietMetadata.height = getFieldAsInt(jpegMetadata, TiffConstants.EXIF_TAG_IMAGE_HEIGHT, TiffConstants.EXIF_TAG_EXIF_IMAGE_LENGTH, TiffConstants.EXIF_TAG_IMAGE_HEIGHT_IFD0);

    sietMetadata.camera = getFieldAsString(jpegMetadata, TiffConstants.EXIF_TAG_MAKE);
    sietMetadata.model = getFieldAsString(jpegMetadata, TiffConstants.EXIF_TAG_MODEL, TiffConstants.EXIF_TAG_MODEL_2);
    sietMetadata.software = getFieldAsString(jpegMetadata, TiffConstants.EXIF_TAG_SOFTWARE);

    sietMetadata.ISO = getFieldAsInt(jpegMetadata, TiffConstants.EXIF_TAG_ISO);

    sietMetadata.exposure = getFieldAsString(jpegMetadata, TiffConstants.EXIF_TAG_EXPOSURE_TIME);

    sietMetadata.aperture = getFieldAsString(jpegMetadata, TiffConstants.EXIF_TAG_APERTURE_VALUE);

    sietMetadata.focalLength = getFieldAsString(jpegMetadata, TiffConstants.EXIF_TAG_FOCAL_LENGTH);

    // @SuppressWarnings("rawtypes")
    // ArrayList items = jpegMetadata.getItems();
    //
    // for (Object object : items) {
    // ImageMetadata.Item item = (ImageMetadata.Item) object;
    // TiffField field = ((TiffImageMetadata.Item) item).getTiffField();
    //
    // System.out.println(item.getKeyword() + " :: " + item.getText() + " :: " +
    // field.tagInfo + " :: " + field.getValue().getClass());
    // }

    return sietMetadata;
  }

  public static void copyMetadataTest(File srcFile, File dstFile) throws IOException, ImageWriteException, ImageReadException {

    File dstFileTmp = new File(dstFile.getParentFile(), "_tmp_" + dstFile.getName());

    TiffOutputSet srcFileOutputSet = ((JpegImageMetadata) Sanselan.getMetadata(srcFile)).getExif().getOutputSet();

    new ExifRewriter().updateExifMetadataLossless(dstFile, new FileOutputStream(dstFileTmp), srcFileOutputSet);

    dstFile.delete();
    dstFileTmp.renameTo(dstFile);
  }

  public static void copyMetadata(File srcFile, File dstFile) throws Throwable {

    File tmpFile = File.createTempFile("SIET.", ".tmp");
    FileOutputStream tmpFileOutputStream = null;

    try {

      tmpFileOutputStream = new FileOutputStream(tmpFile);

      TiffOutputSet srcFileOutputSet = ((JpegImageMetadata) Sanselan.getMetadata(srcFile)).getExif().getOutputSet();

      new ExifRewriter().updateExifMetadataLossless(dstFile, tmpFileOutputStream, srcFileOutputSet);

      tmpFileOutputStream.flush();
    }

    finally {

      if (tmpFileOutputStream != null) {
        tmpFileOutputStream.close();
      }
    }

    // File originalFile = Utils.copyFile(dstFile, new File(dstFile.getAbsolutePath() + ".original"));
    //
    // try {
    // Utils.copyFile(tmpFile, dstFile);
    // originalFile.delete();
    // }
    //
    // catch (IOException e) {
    //
    // Utils.copyFile(originalFile, dstFile);
    // originalFile.delete();
    //
    // throw e;
    // }
    //
    // finally {
    // tmpFile.delete();
    // }
  }

  private static TiffField getField(JpegImageMetadata jpegMetadata, TagInfo... tagInfos) {

    for (TagInfo tagInfo : tagInfos) {

      TiffField field = jpegMetadata.findEXIFValue(tagInfo);

      if (field != null) {
        return field;
      }
    }

    return null;
  }

  private static final DateFormat DF_TIFF_FIELD_VALUE = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

  private static Date getFieldAsDate(JpegImageMetadata jpegMetadata, TagInfo... tagInfos) {
    try {
      return DF_TIFF_FIELD_VALUE.parse(((String) getField(jpegMetadata, tagInfos).getValue()).trim());
    } catch (Throwable t) {
      return null;
    }
  }

  private static int getFieldAsInt(JpegImageMetadata jpegMetadata, TagInfo... tagInfos) {
    try {
      return ((Number) getField(jpegMetadata, tagInfos).getValue()).intValue();
    } catch (Throwable t) {
      return -1;
    }
  }

  private static String getFieldAsString(JpegImageMetadata jpegMetadata, TagInfo... tagInfos) {

    try {

      Object value = getField(jpegMetadata, tagInfos).getValue();

      if (value == null) {
        return "";
      }

      if (value instanceof Number) {
        return ((Number) value).toString();
      }

      if (value instanceof String) {
        return ((String) value).trim();
      }

      else {
        return String.valueOf(value).trim();
      }
    }

    catch (Throwable t) {
      return "";
    }
  }

  public String getFileName() {
    return fileName;
  }

  public long getFileSize() {
    return fileSize;
  }

  public Date getDate() {
    return date;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public String getCamera() {
    return camera;
  }

  public String getModel() {
    return model;
  }

  public String getSoftware() {
    return software;
  }

  public int getISO() {
    return ISO;
  }

  public String getExposure() {
    return exposure;
  }

  public String getAperture() {
    return aperture;
  }

  public String getFocalLength() {
    return focalLength;
  }

}
