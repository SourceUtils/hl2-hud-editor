package com.timepath.tf2.hudedit.loaders;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author andrew
 */
public class VtfLoader {
    
    public VtfLoader() {
        
    }
    
    public static void main(String... args) {
        File root = new File("./res/vtf/");
        File[] subs = root.listFiles();
        for(int i = 0; i < subs.length; i++) {
            if(subs[i].getName().endsWith(".vtf")) {
                new VtfLoader().load(subs[i].getPath());
            }
        }
//        new VtfLoader().load("./res/eng_status_area_sentry_blue.vtf");
//        new VtfLoader().load("./res/bomb_carried.vtf");
    }
    
//      4 bytes of characters
//	unsigned int	version[2];		// version[0].version[1] (currently 7.2).
//	unsigned int	headerSize;		// Size of the header struct (16 byte aligned; currently 80 bytes).
//	unsigned short	width;			// Width of the largest mipmap in pixels. Must be a power of 2.
//	unsigned short	height;			// Height of the largest mipmap in pixels. Must be a power of 2.
//	unsigned int	flags;			// VTF flags.
//	unsigned short	frames;			// Number of frames, if animated (1 for no animation).
//	unsigned short	firstFrame;		// First frame in animation (0 based).
//	unsigned char	padding0[4];		// reflectivity padding (16 byte alignment).
//	float		reflectivity[3];	// reflectivity vector.
//	unsigned char	padding1[4];		// reflectivity padding (8 byte packing).
//	float		bumpmapScale;		// Bumpmap scale.
//	unsigned int	highResImageFormat;	// High resolution image format.
//	unsigned char	mipmapCount;		// Number of mipmaps.
//	unsigned int	lowResImageFormat;	// Low resolution image format (always DXT1).
//	unsigned char	lowResImageWidth;	// Low resolution image width.
//	unsigned char	lowResImageHeight;	// Low resolution image height.
//	unsigned short	depth;			// Depth of the largest mipmap in pixels.
//						// Must be a power of 2. Can be 0 or 1 for a 2D texture (v7.2 only).
    // all Little endian - least significant bits first
    // sometimes the alignment isn't quite right, too far right with previous mipmap visible to the left. particularly obvious on deathwheel images. Some load in different formats, some have LOD tags. Some even load as DXT1!
    // common formats:
    // DXT5, DXT1, BGRA8888, UV88
    // http://www.piksel.org/frei0r/1.1/spec/group__COLOR__MODEL.html
    // http://code.google.com/p/gimp-vtf/source/browse/trunk/gimp-vtf/?r=16
    public void load(String path) {
        RandomAccessFile bin;
        try {
          bin = new RandomAccessFile(path, "r");
          System.out.println("FILE=" + path);
          String signature = new String(new byte[] {readChar(bin), readChar(bin), readChar(bin), readChar(bin)}); // 4
          System.out.println("SIG=" + signature + ", " + (signature.equals("VTF\0") ? "valid" : "invalid"));
          int[] version = {readUInt(bin), readUInt(bin)}; // 12
          System.out.println("VER=" + version[0] + "." + version[1]);
          int headerSize = readUInt(bin); // 16
          System.out.println("LEN=" + headerSize);
          int width = readUShort(bin); // 18
          System.out.println("WIDE=" + width);
          int height = readUShort(bin); // 20
          System.out.println("HIGH=" + height);
          int flags = readUInt(bin); // 24
          System.out.println("FLAG=" + Integer.toHexString(flags));
          
          System.out.println("USEMIPS=" + !((flags & Flags.TEXTUREFLAGS_NOMIP.getMask()) == 0xff));
          
          int frames = readUShort(bin); // 26
          System.out.println("FRAMES=" + frames); // zero indexed
          int first = readUShort(bin); // 28
          System.out.println("FIRSTFRAME=" + first); // zero indexed
          bin.skipBytes(4); // padding to 32
          float[] reflectivity = new float[] {readFloat(bin), readFloat(bin), readFloat(bin)}; // 44
          System.out.println("REFLECTIVITY=" + reflectivity[0] + ", " + reflectivity[1] + ", " + reflectivity[2]);
          bin.skipBytes(4); // padding 48
          float bumpScale = readFloat(bin); // 52
          System.out.println("BUMPSCALE=" + bumpScale);
          int fullFormat = readUInt(bin); // 56
          System.out.println("FULLFORMAT=" + Formats.getEnumForIndex(fullFormat));
          int mipCount = readUChar(bin); // 57
          System.out.println("MIPS=" + mipCount);
          int lowFormat = readUInt(bin); // 61
          System.out.println("LOWFORMAT=" + Formats.getEnumForIndex(lowFormat));
          int lowWidth = readUChar(bin); // 62
          System.out.println("LOWWIDTH=" + lowWidth);
          int lowHeight = readUChar(bin); // 63
          System.out.println("LOWHIGH=" + lowHeight);
          int depth = readUByte(bin); // 64. documentation says this is 2 bytes, but I think that they are wrong
          System.out.println("DEPTH=" + depth);
          // http://msdn.microsoft.com/en-us/library/aa920432.aspx
          
          bin.skipBytes(headerSize - 64 - 8); // 64 for above info, 8 for CRC. I have no idea what the data inbetween does          
          
          String crcHead = new String(new byte[] {readChar(bin), readChar(bin), readChar(bin), readChar(bin)});
          System.out.println("CRC=" + crcHead + ", " + (crcHead.equals("CRC\2") ? "valid" : "invalid") + " tag");
          int crc = readLong(bin);
          System.out.println("CRC=0x" + Integer.toHexString(crc).toUpperCase());
          
          int lowPieces = (lowWidth * lowHeight) / 16; // DXT1. Each 'block' is 4*4 pixels. 16 pixels become 8 bytes
          System.out.println("LOWPIECES=" + lowPieces);
          byte[] thumbData = new byte[lowPieces * 8]; // 8 bytes per piece
          bin.read(thumbData);
          BufferedImage thumbImage = loadDXT1(thumbData, lowWidth, lowHeight); // no mipmaps
          
          JFrame f = new JFrame(path);
          f.setLayout(new BorderLayout());
          
          BufferedImage image = null;
          for(int i = 0; i < mipCount; i++) { // mipmaps
              int w = width;
              int h = height;
              
              for(int n = 1; n < mipCount - i; n++) { // do not do on biggest
                  w /= 2;
                  h /= 2;
              }
              
              int pieces = (w * h) / 16;
              System.out.println("HIGHPIECES=" + pieces + ", MIP="+(mipCount-i-1));
              byte[] imageData = new byte[pieces * (((2 * 8) + (4*4*3) + (2 * 16) + (4*4*2)) / 8)]; // DXT5. Each 'block' is 4*4 pixels + some other data. 16 pixels become 16 bytes [128 bits] (2 * 8 bit alpha values, 4x4 3 bit alpha indicies, 2 * 16 bit colours, 4*4 2 bit indicies)
              bin.read(imageData);
              if(w <= 0) {
                  w = 1;
              }
              if(h <= 0) {
                  h = 1;
              }
              image = loadDXT5(imageData, w, h);
          }
          f.add(new JLabel(new ImageIcon(image)), BorderLayout.SOUTH);
          
          f.add(new JLabel(new ImageIcon(thumbImage)), BorderLayout.NORTH);
          
          f.setVisible(true);
          f.pack();
          
          System.out.println("L:" + bin.length() + ", P:" + bin.getFilePointer() + ", R:" + (bin.length() - bin.getFilePointer()));
          System.out.print("\n\n\n");
          
          bin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    byte readByte(RandomAccessFile f) throws IOException {
        byte b = f.readByte();
//        System.err.println(Integer.toHexString(new Byte(b)).toUpperCase());
        return b;
    }
    
    int readUByte(RandomAccessFile f) throws IOException {
        return readByte(f) & 0xff;
    }
    
    byte readChar(RandomAccessFile f) throws IOException {
        return readByte(f);
    }
    
    int readUChar(RandomAccessFile f) throws IOException {
        return readUByte(f);
    }
    
    int readUInt(RandomAccessFile f) throws IOException {
        return readUByte(f) + (readUByte(f) << 8) + (readUByte(f) << 16) + (readUByte(f) << 24);
    }
    
    float readFloat(RandomAccessFile f) throws IOException {
        int intBits = readUByte(f) + (readUByte(f) << 8) + (readUByte(f) << 16) + (readUByte(f) << 24);
        return Float.intBitsToFloat(intBits);
    }
    
    int readUShort(RandomAccessFile f) throws IOException {
        return readUByte(f) + (readUByte(f) << 8);
    }
    
    int readLong(RandomAccessFile f) throws IOException {
        return readByte(f) + (readByte(f) << 8) + (readByte(f) << 16) + (readByte(f) << 24);
    }
    
    String toBinaryString(byte n) {
        StringBuilder sb = new StringBuilder("00000000");
        for (int bit = 0; bit < 8; bit++) {
            if (((n >> bit) & 1) > 0) {
                sb.setCharAt(7 - bit, '1');
            }
        }
        return sb.toString();
    }
    
    /**
     * http://en.wikipedia.org/wiki/S3_Texture_Compression
     * http://www.fsdeveloper.com/wiki/index.php?title=DXT_compression_explained
     * http://msdn.microsoft.com/en-us/library/aa920432.aspx
     * 
     * 8 bytes per 4*4
     */
    BufferedImage loadDXT1(byte[] b, int width, int height) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();
        int pos = 0;
        
        int red_mask_565 = 0xF800; // first 5 bits
        int green_mask_565 = 0x7E0; // next 6 bits
        int blue_mask_565 = 0x1F;
        
        int red_mask_555 = 0x7C00; // first 5 bits
        int green_mask_555 = 0x3E0; // next 5 bits
        int blue_mask_555 = 0x1F;
        
        int alpha_mask_555 = 0x1;
        
        int bits_12 = 0xC0; // first 2 bits
        int bits_34 = 0x30; // next 2 bits
        int bits_56 = 0xC; // next 2 bits
        int bits_78 = 0x3; // last 2 bits

//        RGB 565: WORD pixel565 = (red_value << 11) | (green_value << 5) | blue_value;
//        RGB 555: WORD pixel565 = (red << 10) | (green << 5) | blue;
        
        for(int y = 0; y < (height / 4); y++) {
            for(int x = 0; x < (width / 4); x++) {
                int color_0 = (b[pos++] & 0xff) + ((b[pos++] & 0xff) << 8); // 2 bytes
                int color_1 = (b[pos++] & 0xff) + ((b[pos++] & 0xff) << 8); // 2 bytes
                
                int red1, green1, blue1, red2, green2, blue2;
                Color c1, c2;
                
                if(color_0 < color_1) { // 3 colours + transparency : 5551 rgba
                    red1   = (int) (((color_0 & red_mask_555) >> 10) << 3);
                    green1 = (int) (((color_0 & green_mask_555) >> 5) << 3);
                    blue1  = (int) ((color_0 & blue_mask_555) << 3);
                    int alpha1 = (int) ((color_0 & alpha_mask_555) << 7);
                    c1 = new Color(red1, green1, blue1, alpha1);

                    red2   = (int) (((color_1 & red_mask_555) >> 10) << 3);
                    green2 = (int) (((color_1 & green_mask_555) >> 5) << 3);
                    blue2  = (int) ((color_1 & alpha_mask_555) << 7);
                    int alpha2 = 0;
                    c2 = new Color(red2, green2, blue2, alpha2);
                } else { // 4 colours : 565 rgb
                    red1   = (int) (((color_0 & red_mask_565) >> 11) << 3);
                    green1 = (int) (((color_0 & green_mask_565) >> 5) << 2);
                    blue1  = (int) ((color_0 & blue_mask_565) << 3);
                    c1 = new Color(red1, green1, blue1);

                    red2   = (int) (((color_1 & red_mask_565) >> 11) << 3);
                    green2 = (int) (((color_1 & green_mask_565) >> 5) << 2);
                    blue2  = (int) ((color_1 & blue_mask_565) << 3);
                    c2 = new Color(red2, green2, blue2);
                }

                
                
                // remaining 4 bytes
                for(int y1 = 0; y1 < 4; y1++) { // 16 bits / 4 lines = 4 bits/line = 1 byte/line
                    byte next4 = b[pos++];
                    int[] bits = new int[]{(next4 & bits_12) >> 6, (next4 & bits_34) >> 4, (next4 & bits_56) >> 2, next4 & bits_78};
                    
                    for(int i = 0; i < 4; i++) { // horizontal scan
                        int bit = bits[i];
                        if(bit == 0) {
                            g.setColor(c1);
                        } else if(bit == 1) {
                            g.setColor(c2);
                        } else if(color_0 < color_1) { // transparent
                            if(bit == 2) {                            
                                int cred = (c1.getRed() / 2) + (c2.getRed() / 2);
                                int cgrn = (c1.getGreen() / 2) + (c2.getGreen() / 2);
                                int cblu = (c1.getBlue() / 2) + (c2.getBlue() / 2);
                                int calp = (c1.getAlpha() / 2) + (c2.getAlpha() / 2);
                                Color c = new Color(cred, cgrn, cblu, calp);
                                g.setColor(c);
                            } else if(bit == 3) {
                                Color c = new Color(0, 0, 0, 0); // transparent
                                g.setColor(c);
                            }
                        } else {
                            if(bit == 2) {                            
                                int cred = (2 * (c1.getRed() / 3)) + (c2.getRed() / 3);
                                int cgrn = (2 * (c1.getGreen() / 3)) + (c2.getGreen() / 3);
                                int cblu = (2 * (c1.getBlue() / 3)) + (c2.getBlue() / 3);
                                Color c = new Color(cred, cgrn, cblu);
                                g.setColor(c);
                            } else if(bit == 3) {
                                int cred = (c1.getRed() / 3) + (2 * (c2.getRed() / 3));
                                int cgrn = (c1.getGreen() / 3) + (2 * (c2.getGreen() / 3));
                                int cblu = (c1.getBlue() / 3) + (2 * (c2.getBlue() / 3));
                                Color c = new Color(cred, cgrn, cblu);
                                g.setColor(c);
                            }
                        }
                        g.drawLine((x * 4) + 4 - i, (y * 4) + y1,
                                   (x * 4) + 4 - i, (y * 4) + y1);
                    }
                }
            }
        }
        return bi;
    }
    
    /**
     * http://en.wikipedia.org/wiki/S3_Texture_Compression
     * http://www.fsdeveloper.com/wiki/index.php?title=DXT_compression_explained
     * http://msdn.microsoft.com/en-us/library/aa920432.aspx
     * 
     * 8 bytes for alpha channel, additional 8 per 4*4 chunk
     */
    BufferedImage loadDXT5(byte[] b, int width, int height) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();
        int pos = 0;
        
        int red_mask_565 = 0xF800; // first 5 bits
        int green_mask_565 = 0x7E0; // next 6 bits
        int blue_mask_565 = 0x1F;
        
        int bits_12 = 0xC0; // first 2 bits
        int bits_34 = 0x30; // next 2 bits
        int bits_56 = 0xC; // next 2 bits
        int bits_78 = 0x3; // last 2 bits

//        RGB 565: WORD pixel565 = (red_value << 11) | (green_value << 5) | blue_value;
        
        for(int y = 0; y < (height / 4); y++) {
            for(int x = 0; x < (width / 4); x++) {
                pos += 8; // 64 bits of alpha channel data (two 8 bit alpha values and a 4x4 3 bit lookup table) 
                
                int color_0 = (b[pos++] & 0xff) + ((b[pos++] & 0xff) << 8); // 2 bytes
                int color_1 = (b[pos++] & 0xff) + ((b[pos++] & 0xff) << 8); // 2 bytes
                
                int red1, green1, blue1, red2, green2, blue2;
                Color c1, c2;
                
                red1 = (int) (((color_0 & red_mask_565) >> 11) << 3);
                green1 = (int) (((color_0 & green_mask_565) >> 5) << 2);
                blue1 = (int) ((color_0 & blue_mask_565) << 3);
                c1 = new Color(red1, green1, blue1);

                red2 = (int) (((color_1 & red_mask_565) >> 11) << 3);
                green2 = (int) (((color_1 & green_mask_565) >> 5) << 2);
                blue2 = (int) ((color_1 & blue_mask_565) << 3);
                c2 = new Color(red2, green2, blue2);
                
                // remaining 4 bytes
                for(int y1 = 0; y1 < 4; y1++) { // 16 bits / 4 lines = 4 bits/line = 1 byte/line
                    byte next4 = b[pos++];
                    int[] bits = new int[]{(next4 & bits_12) >> 6, (next4 & bits_34) >> 4, (next4 & bits_56) >> 2, next4 & bits_78};
                    
                    for(int i = 0; i < 4; i++) { // horizontal scan
                        int bit = bits[i];
                        if(bit == 0) {
                            g.setColor(c1);
                        } else if(bit == 1) {
                            g.setColor(c2);
                        } else if(bit == 2) {                            
                            int cred = (2 * (c1.getRed() / 3)) + (c2.getRed() / 3);
                            int cgrn = (2 * (c1.getGreen() / 3)) + (c2.getGreen() / 3);
                            int cblu = (2 * (c1.getBlue() / 3)) + (c2.getBlue() / 3);
                            Color c = new Color(cred, cgrn, cblu);
                            g.setColor(c);
                        } else if(bit == 3) {
                            int cred = (c1.getRed() / 3) + (2 * (c2.getRed() / 3));
                            int cgrn = (c1.getGreen() / 3) + (2 * (c2.getGreen() / 3));
                            int cblu = (c1.getBlue() / 3) + (2 * (c2.getBlue() / 3));
                            Color c = new Color(cred, cgrn, cblu);
                            g.setColor(c);
                        }
                        g.drawLine((x * 4) + 4 - i, (y * 4) + y1,
                                   (x * 4) + 4 - i, (y * 4) + y1);
                    }
                }
            }
        }
        return bi;
    }
    
    private static enum Formats {
	IMAGE_FORMAT_NONE(-1),
	IMAGE_FORMAT_RGBA8888(0),
	IMAGE_FORMAT_ABGR8888(1),
	IMAGE_FORMAT_RGB888(2),
	IMAGE_FORMAT_BGR888(3),
	IMAGE_FORMAT_RGB565(4),
	IMAGE_FORMAT_I8(5),
	IMAGE_FORMAT_IA88(6),
	IMAGE_FORMAT_P8(7),
	IMAGE_FORMAT_A8(8),
	IMAGE_FORMAT_RGB888_BLUESCREEN(9),
	IMAGE_FORMAT_BGR888_BLUESCREEN(10),
	IMAGE_FORMAT_ARGB8888(11),
	IMAGE_FORMAT_BGRA8888(12),
	IMAGE_FORMAT_DXT1(13),
	IMAGE_FORMAT_DXT3(14),
	IMAGE_FORMAT_DXT5(15),
	IMAGE_FORMAT_BGRX8888(16),
	IMAGE_FORMAT_BGR565(17),
	IMAGE_FORMAT_BGRX5551(18),
	IMAGE_FORMAT_BGRA4444(19),
	IMAGE_FORMAT_DXT1_ONEBITALPHA(20),
	IMAGE_FORMAT_BGRA5551(21),
	IMAGE_FORMAT_UV88(22),
	IMAGE_FORMAT_UVWQ8888(23),
	IMAGE_FORMAT_RGBA16161616F(24),
	IMAGE_FORMAT_RGBA16161616(25),
	IMAGE_FORMAT_UVLX8888(26);
        
        private int index;
        
        private Formats(int index) {
            this.index = index;
        }
        
        public int getIndex() {
            return index;
        }
        
        public static Formats getEnumForIndex(int index) {
            Formats[] values = Formats.values();
            for(Formats eachValue : values) {
                if(eachValue.getIndex() == index) {
                    return eachValue;
                }
            }
            return null;
        }
        
    }
    
    /**
     * https://developer.valvesoftware.com/wiki/Valve_Texture_Format#Image_flags
     */
    private static enum Flags {
        // Flags from the *.txt config file
        TEXTUREFLAGS_POINTSAMPLE(0x00000001),
        TEXTUREFLAGS_TRILINEAR(0x00000002),
        TEXTUREFLAGS_CLAMPS(0x00000004),
        TEXTUREFLAGS_CLAMPT(0x00000008),
        TEXTUREFLAGS_ANISOTROPIC(0x00000010),
        TEXTUREFLAGS_HINT_DXT5(0x00000020),
        TEXTUREFLAGS_PWL_CORRECTED(0x00000040),
        TEXTUREFLAGS_NORMAL(0x00000080),
        TEXTUREFLAGS_NOMIP(0x00000100),
        TEXTUREFLAGS_NOLOD(0x00000200),
        TEXTUREFLAGS_ALL_MIPS(0x00000400),
        TEXTUREFLAGS_PROCEDURAL(0x00000800),

        // These are automatically generated by vtex from the texture data.
        TEXTUREFLAGS_ONEBITALPHA(0x00001000),
        TEXTUREFLAGS_EIGHTBITALPHA(0x00002000),

        // Newer flags from the *.txt config file
        TEXTUREFLAGS_ENVMAP(0x00004000),
        TEXTUREFLAGS_RENDERTARGET(0x00008000),
        TEXTUREFLAGS_DEPTHRENDERTARGET(0x00010000),
        TEXTUREFLAGS_NODEBUGOVERRIDE(0x00020000),
        TEXTUREFLAGS_SINGLECOPY(0x00040000),
        TEXTUREFLAGS_PRE_SRGB(0x00080000),

        TEXTUREFLAGS_UNUSED_00100000(0x00100000),
        TEXTUREFLAGS_UNUSED_00200000(0x00200000),
        TEXTUREFLAGS_UNUSED_00400000(0x00400000),

        TEXTUREFLAGS_NODEPTHBUFFER(0x00800000),

        TEXTUREFLAGS_UNUSED_01000000(0x01000000),

        TEXTUREFLAGS_CLAMPU(0x02000000),
        TEXTUREFLAGS_VERTEXTEXTURE(0x04000000),
        TEXTUREFLAGS_SSBUMP(0x08000000),			

        TEXTUREFLAGS_UNUSED_10000000(0x10000000),

        TEXTUREFLAGS_BORDER(0x20000000),

        TEXTUREFLAGS_UNUSED_40000000(0x40000000),
        TEXTUREFLAGS_UNUSED_80000000(0x80000000);

        private int mask;

        private Flags(int mask) {
            this.mask = mask;
        }

        public int getMask() {
            return mask;
        }

        public static Flags getEnumForMask(int mask) {
            Flags[] values = Flags.values();
            for(Flags eachValue : values) {
                if(eachValue.getMask() == mask) {
                    return eachValue;
                }
            }
            return null;
        }
    }

}
