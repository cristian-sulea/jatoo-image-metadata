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

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.util.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class JaTooImageMetadataUtilsTests {

  @Test
  public void test() throws Throwable {

    File srcImageFile = new File("target", "20141109144518-0.jpg");
    File dstImageFile = new File("target", "20141109144518-400x300.jpg");

    IOUtils.copyStreamToStream(getClass().getResourceAsStream(srcImageFile.getName()), new FileOutputStream(srcImageFile));
    IOUtils.copyStreamToStream(getClass().getResourceAsStream(dstImageFile.getName()), new FileOutputStream(dstImageFile));

    JaTooImageMetadataUtils.copyMetadata(srcImageFile, dstImageFile);

    JpegImageMetadata srcImageMetadata = (JpegImageMetadata) Sanselan.getMetadata(srcImageFile);
    JpegImageMetadata dstImageMetadata = (JpegImageMetadata) Sanselan.getMetadata(dstImageFile);

    Assert.assertNotNull(srcImageMetadata);
    Assert.assertNotNull(dstImageMetadata);

    Assert.assertEquals(JaTooImageMetadataUtils.getMetadataDate(srcImageFile), JaTooImageMetadataUtils.getMetadataDate(dstImageFile));

    assertFieldEquals(srcImageMetadata, dstImageMetadata, TiffConstants.EXIF_TAG_CREATE_DATE);
    assertFieldEquals(srcImageMetadata, dstImageMetadata, TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
    assertFieldEquals(srcImageMetadata, dstImageMetadata, TiffConstants.EXIF_TAG_MODIFY_DATE);

    assertFieldEquals(srcImageMetadata, dstImageMetadata, TiffConstants.EXIF_TAG_IMAGE_WIDTH);
    assertFieldEquals(srcImageMetadata, dstImageMetadata, TiffConstants.EXIF_TAG_EXIF_IMAGE_WIDTH);
    assertFieldEquals(srcImageMetadata, dstImageMetadata, TiffConstants.EXIF_TAG_IMAGE_WIDTH_IFD0);

    assertFieldEquals(srcImageMetadata, dstImageMetadata, TiffConstants.EXIF_TAG_IMAGE_HEIGHT);
    assertFieldEquals(srcImageMetadata, dstImageMetadata, TiffConstants.EXIF_TAG_EXIF_IMAGE_LENGTH);
    assertFieldEquals(srcImageMetadata, dstImageMetadata, TiffConstants.EXIF_TAG_IMAGE_HEIGHT_IFD0);

    assertFieldEquals(srcImageMetadata, dstImageMetadata, TiffConstants.EXIF_TAG_MAKE);

    assertFieldEquals(srcImageMetadata, dstImageMetadata, TiffConstants.EXIF_TAG_MODEL);
    assertFieldEquals(srcImageMetadata, dstImageMetadata, TiffConstants.EXIF_TAG_MODEL_2);

    assertFieldEquals(srcImageMetadata, dstImageMetadata, TiffConstants.EXIF_TAG_SOFTWARE);
  }

  private void assertFieldEquals(JpegImageMetadata srcImageMetadata, JpegImageMetadata dstImageMetadata, TagInfo field) throws ImageReadException {

    TiffField srcField = srcImageMetadata.findEXIFValue(field);
    TiffField dstField = dstImageMetadata.findEXIFValue(field);

    if (srcField != null && dstField != null) {
      Assert.assertEquals(srcField.getValue(), dstField.getValue());
    }
  }

}
