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
import java.text.ParseException;
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
 * A collection of utility methods to ease the work with images metadata.
 * 
 * @author <a href="http://cristian.sulea.net" rel="author">Cristian Sulea</a>
 * @version 1.0, October 5, 2016
 */
public final class JaTooImageMetadataUtils {

  /** the logger */
  // private static final Log logger = LogFactory.getLog(JaTooImageMetadata.class);

  /**
   * Utility classes should not have a public or default constructor.
   */
  private JaTooImageMetadataUtils() {}

  /**
   * Copy (replace) the EXIF metadata from the one Jpeg image into another. Note that the destination file will be
   * deleted and replaced with a new file (metadata + image).
   * 
   * @param srcImageFile
   *          source image file with the EXIF metadata to be copied
   * @param dstImageFile
   *          destination image file to have the EXIF metadata replaced
   * 
   * @throws ImageReadException
   * @throws ImageWriteException
   * @throws IOException
   */
  public static void copyMetadata(File srcImageFile, File dstImageFile) throws ImageReadException, ImageWriteException, IOException {

    File tmpFile = File.createTempFile("jatoo-image-metadata.", ".tmp");
    try (FileOutputStream tmpFileOutputStream = new FileOutputStream(tmpFile)) {

      TiffOutputSet srcFileOutputSet = ((JpegImageMetadata) Sanselan.getMetadata(srcImageFile)).getExif().getOutputSet();

      new ExifRewriter().updateExifMetadataLossless(dstImageFile, tmpFileOutputStream, srcFileOutputSet);

      tmpFileOutputStream.close();
    }

    dstImageFile.delete();
    tmpFile.renameTo(dstImageFile);
  }

  /**
   * Tries to find the image date taken in the metadata of the provided image.
   * 
   * @param image
   *          the image file with the EXIF metadata
   * 
   * @return the so called image date taken
   * 
   * @throws ImageReadException
   * @throws IOException
   * @throws ParseException
   */
  public static Date getMetadataDate(File image) throws ImageReadException, IOException, ParseException {

    JpegImageMetadata metadata = (JpegImageMetadata) Sanselan.getMetadata(image);

    Object dateObject = getMetadataField(metadata, TiffConstants.EXIF_TAG_CREATE_DATE, TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL, TiffConstants.EXIF_TAG_MODIFY_DATE);
    String dateString = ((String) dateObject).trim();

    return new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(dateString);
  }

  public static Object getMetadataField(JpegImageMetadata metadata, TagInfo... tags) throws ImageReadException {

    for (TagInfo tag : tags) {

      TiffField field = metadata.findEXIFValue(tag);

      if (field != null) {
        return field.getValue();
      }
    }

    return null;
  }

}
