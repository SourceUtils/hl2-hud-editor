package com.timepath.tf2.hudedit.loaders;

import java.io.RandomAccessFile;

/**
 *
 * @author andrew
 */
public class VtfLoader {
    
    public VtfLoader() {
        
    }
    
    public static void main(String... args) {
        new VtfLoader().load("./res/eng_status_area_sentry_blue.vtf");
    }
    
    
//    typedef struct tagVTFHEADER
//{
//	char		signature[4];		// 
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

    // http://en.wikipedia.org/wiki/C_data_types#Basic_types
    // http://www.cafeaulait.org/course/week2/02.html
    public void load(String path) {
        RandomAccessFile bin;

        try {
          bin = new RandomAccessFile(path, "r");
          String signature = new String(new byte[] {bin.readByte(), bin.readByte(), bin.readByte(), bin.readByte()});
          System.out.println("SIG=" + signature + ", " + (signature.equals("VTF\0") ? "valid" : "invalid"));
          int[] version = {bin.readUnsignedShort(), bin.readUnsignedShort()};
          System.out.println("VER=" + version[0] + "." + version[1]);
          int headerSize = bin.readInt();
          System.out.println("LEN=" + headerSize);
          
          bin.close();
        } catch (Exception e) {
          System.out.println("**Error: " + e.getMessage());
        }
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
