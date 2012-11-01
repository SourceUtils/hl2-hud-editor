typedef struct
{
    char		signature[4];		// File signature ("VTF\0").
    unsigned int	version[2];		// version[0].version[1] (currently 7.2).
    unsigned int	headerSize;		// Size of the header struct (16 byte aligned; currently 80 bytes).
    unsigned short	width;			// Width of the largest mipmap in pixels. Must be a power of 2.
    unsigned short	height;			// Height of the largest mipmap in pixels. Must be a power of 2.
    unsigned int	flags;			// VTF flags.
    unsigned short	frames;			// Number of frames, if animated (1 for no animation).
    unsigned short	firstFrame;		// First frame in animation (0 based).
    unsigned char	padding0[4];		// reflectivity padding (16 byte alignment).
    float		reflectivity[3];	// reflectivity vector.
    unsigned char	padding1[4];		// reflectivity padding (8 byte packing).
    float		bumpmapScale;		// Bumpmap scale.
    unsigned int	highResImageFormat;	// High resolution image format.
    unsigned char	mipmapCount;		// Number of mipmaps.
    unsigned int	lowResImageFormat;	// Low resolution image format (always DXT1).
    unsigned char	lowResImageWidth;	// Low resolution image width.
    unsigned char	lowResImageHeight;	// Low resolution image height.
    unsigned short	depth;			// Depth of the largest mipmap in pixels.
    					// Must be a power of 2. Can be 0 or 1 for a 2D texture (v7.2 only).
} VtfHeader;

enum
{
    IMAGE_FORMAT_NONE = -1,
    IMAGE_FORMAT_RGBA8888 = 0,
    IMAGE_FORMAT_ABGR8888,
    IMAGE_FORMAT_RGB888,
    IMAGE_FORMAT_BGR888,
    IMAGE_FORMAT_RGB565,
    IMAGE_FORMAT_I8,
    IMAGE_FORMAT_IA88,
    IMAGE_FORMAT_P8,
    IMAGE_FORMAT_A8,
    IMAGE_FORMAT_RGB888_BLUESCREEN,
    IMAGE_FORMAT_BGR888_BLUESCREEN,
    IMAGE_FORMAT_ARGB8888,
    IMAGE_FORMAT_BGRA8888,
    IMAGE_FORMAT_DXT1,
    IMAGE_FORMAT_DXT3,
    IMAGE_FORMAT_DXT5,
    IMAGE_FORMAT_BGRX8888,
    IMAGE_FORMAT_BGR565,
    IMAGE_FORMAT_BGRX5551,
    IMAGE_FORMAT_BGRA4444,
    IMAGE_FORMAT_DXT1_ONEBITALPHA,
    IMAGE_FORMAT_BGRA5551,
    IMAGE_FORMAT_UV88,
    IMAGE_FORMAT_UVWQ8888,
    IMAGE_FORMAT_RGBA16161616F,
    IMAGE_FORMAT_RGBA16161616,
    IMAGE_FORMAT_UVLX8888
};

typedef struct
{
    GdkPixbufModuleSizeFunc     size_func;
    GdkPixbufModulePreparedFunc prepared_func; 
    GdkPixbufModuleUpdatedFunc  updated_func;
    gpointer                    user_data;

    guchar *buffer;
    guint buffer_size;
    guint buffer_data_size;
} VtfContext;

static gboolean
gdk_pixbuf__vtf_image_stop_load (gpointer context_ptr, GError **error)
{
    VtfContext *context = (VtfContext *) context_ptr;

    
    VtfHeader header;
    memcpy(&header, context->buffer, sizeof(header));
    
    if(header.signature[0] != 'V' || header.signature[1] != 'T' || header.signature[2] != 'F' || header.signature[3] != 0) {
    	g_set_error (
    		error,
    		GDK_PIXBUF_ERROR,
    		GDK_PIXBUF_ERROR_CORRUPT_IMAGE,
    		("File corrupt or incomplete"));
        return FALSE;
    }
    
    int i, j;
    GdkPixbuf* pixbuf;
    
    if(header.highResImageFormat == IMAGE_FORMAT_RGBA8888) {
        pixbuf = gdk_pixbuf_new(GDK_COLORSPACE_RGB, TRUE, 8, header.width, header.height);
        uint32_t stride = gdk_pixbuf_get_rowstride(pixbuf);
        guchar* pixels = gdk_pixbuf_get_pixels(pixbuf);
        int pos = context->buffer_data_size - header.width * header.height * 4;
        for (i = 0; i < header.height; i++)
        	for (j = 0; j < header.width; j++) {
                pixels[stride*i + 4*j + 0] = context->buffer[pos++];
                pixels[stride*i + 4*j + 1] = context->buffer[pos++];
                pixels[stride*i + 4*j + 2] = context->buffer[pos++];
                pixels[stride*i + 4*j + 3] = context->buffer[pos++];
        	}
    } else if(header.highResImageFormat == IMAGE_FORMAT_ABGR8888) {
        pixbuf = gdk_pixbuf_new(GDK_COLORSPACE_RGB, TRUE, 8, header.width, header.height);
        uint32_t stride = gdk_pixbuf_get_rowstride(pixbuf);
        guchar* pixels = gdk_pixbuf_get_pixels(pixbuf);
        int pos = context->buffer_data_size - header.width * header.height * 4;
        for (i = 0; i < header.height; i++)
        	for (j = 0; j < header.width; j++) {
                pixels[stride*i + 4*j + 3] = context->buffer[pos++];
                pixels[stride*i + 4*j + 2] = context->buffer[pos++];
                pixels[stride*i + 4*j + 1] = context->buffer[pos++];
                pixels[stride*i + 4*j + 0] = context->buffer[pos++];
        	}
    } else if(header.highResImageFormat == IMAGE_FORMAT_RGB888) {
        pixbuf = gdk_pixbuf_new(GDK_COLORSPACE_RGB, FALSE, 8, header.width, header.height);
        uint32_t stride = gdk_pixbuf_get_rowstride(pixbuf);
        guchar* pixels = gdk_pixbuf_get_pixels(pixbuf);
        int pos = context->buffer_data_size - header.width * header.height * 3;
        for (i = 0; i < header.height; i++)
        	for (j = 0; j < header.width; j++) {
                pixels[stride*i + 3*j + 0] = context->buffer[pos++];
                pixels[stride*i + 3*j + 1] = context->buffer[pos++];
                pixels[stride*i + 3*j + 2] = context->buffer[pos++];
        	}
    } else if(header.highResImageFormat == IMAGE_FORMAT_BGR888) {
        pixbuf = gdk_pixbuf_new(GDK_COLORSPACE_RGB, FALSE, 8, header.width, header.height);
        uint32_t stride = gdk_pixbuf_get_rowstride(pixbuf);
        guchar* pixels = gdk_pixbuf_get_pixels(pixbuf);
        int pos = context->buffer_data_size - header.width * header.height * 3;
        for (i = 0; i < header.height; i++)
        	for (j = 0; j < header.width; j++) {
                pixels[stride*i + 3*j + 2] = context->buffer[pos++];
                pixels[stride*i + 3*j + 1] = context->buffer[pos++];
                pixels[stride*i + 3*j + 0] = context->buffer[pos++];
        	}
    } else if(header.highResImageFormat == IMAGE_FORMAT_RGB565) {
        pixbuf = gdk_pixbuf_new(GDK_COLORSPACE_RGB, FALSE, 8, header.width, header.height);
        uint32_t stride = gdk_pixbuf_get_rowstride(pixbuf);
        guchar* pixels = gdk_pixbuf_get_pixels(pixbuf);
        int pos = context->buffer_data_size - header.width * header.height * 2;
        for (i = 0; i < header.height; i++)
        	for (j = 0; j < header.width; j++) {
        	    uint16_t c0 = context->buffer[pos++];
        	    c0 |= context->buffer[pos++] << 8;
        	    
        	    uint16_t r, g, b;
        	    r = (c0 >> 11) & 31;
        	    r = (r << 3) | (r >> 5);
        	    g = (c0 >> 5) & 63;
        	    g = (g << 2) | (g >> 6);
        	    b = (c0 >> 0) & 31;
        	    b = (b << 3) | (b >> 5);
        	    
                pixels[stride*i + 3*j + 0] = r;
                pixels[stride*i + 3*j + 1] = g;
                pixels[stride*i + 3*j + 2] = b;
        	}
    } else if(header.highResImageFormat == IMAGE_FORMAT_I8) {
        pixbuf = gdk_pixbuf_new(GDK_COLORSPACE_RGB, FALSE, 8, header.width, header.height);
        uint32_t stride = gdk_pixbuf_get_rowstride(pixbuf);
        guchar* pixels = gdk_pixbuf_get_pixels(pixbuf);
        int pos = context->buffer_data_size - header.width * header.height * 1;
        for (i = 0; i < header.height; i++)
        	for (j = 0; j < header.width; j++) {
        	    uint8_t v = context->buffer[pos++];
        	    
                pixels[stride*i + 3*j + 0] = v;
                pixels[stride*i + 3*j + 1] = v;
                pixels[stride*i + 3*j + 2] = v;
        	}
    } else if(header.highResImageFormat == IMAGE_FORMAT_IA88) {
        pixbuf = gdk_pixbuf_new(GDK_COLORSPACE_RGB, TRUE, 8, header.width, header.height);
        uint32_t stride = gdk_pixbuf_get_rowstride(pixbuf);
        guchar* pixels = gdk_pixbuf_get_pixels(pixbuf);
        int pos = context->buffer_data_size - header.width * header.height * 2;
        for (i = 0; i < header.height; i++)
        	for (j = 0; j < header.width; j++) {
        	    uint8_t v = context->buffer[pos++];
        	    
                pixels[stride*i + 4*j + 0] = v;
                pixels[stride*i + 4*j + 1] = v;
                pixels[stride*i + 4*j + 2] = v;
                pixels[stride*i + 4*j + 3] = context->buffer[pos++];
        	}
    } else if(header.highResImageFormat == IMAGE_FORMAT_A8) {
        pixbuf = gdk_pixbuf_new(GDK_COLORSPACE_RGB, TRUE, 8, header.width, header.height);
        uint32_t stride = gdk_pixbuf_get_rowstride(pixbuf);
        guchar* pixels = gdk_pixbuf_get_pixels(pixbuf);
        int pos = context->buffer_data_size - header.width * header.height * 1;
        for (i = 0; i < header.height; i++)
        	for (j = 0; j < header.width; j++) {
                pixels[stride*i + 4*j + 0] = 255;
                pixels[stride*i + 4*j + 1] = 255;
                pixels[stride*i + 4*j + 2] = 255;
                pixels[stride*i + 4*j + 3] = context->buffer[pos++];
        	}
    } else if(header.highResImageFormat == IMAGE_FORMAT_ARGB8888) {
        pixbuf = gdk_pixbuf_new(GDK_COLORSPACE_RGB, TRUE, 8, header.width, header.height);
        uint32_t stride = gdk_pixbuf_get_rowstride(pixbuf);
        guchar* pixels = gdk_pixbuf_get_pixels(pixbuf);
        int pos = context->buffer_data_size - header.width * header.height * 4;
        for (i = 0; i < header.height; i++)
        	for (j = 0; j < header.width; j++) {
                pixels[stride*i + 4*j + 1] = context->buffer[pos++];
                pixels[stride*i + 4*j + 2] = context->buffer[pos++];
                pixels[stride*i + 4*j + 3] = context->buffer[pos++];
                pixels[stride*i + 4*j + 0] = context->buffer[pos++];
        	}
    } else if(header.highResImageFormat == IMAGE_FORMAT_BGRA8888) {
        pixbuf = gdk_pixbuf_new(GDK_COLORSPACE_RGB, TRUE, 8, header.width, header.height);
        uint32_t stride = gdk_pixbuf_get_rowstride(pixbuf);
        guchar* pixels = gdk_pixbuf_get_pixels(pixbuf);
        int pos = context->buffer_data_size - header.width * header.height * 4;
        for (i = 0; i < header.height; i++)
        	for (j = 0; j < header.width; j++) {
                pixels[stride*i + 4*j + 2] = context->buffer[pos++];
                pixels[stride*i + 4*j + 1] = context->buffer[pos++];
                pixels[stride*i + 4*j + 0] = context->buffer[pos++];
                pixels[stride*i + 4*j + 3] = context->buffer[pos++];
        	}
    } else if(header.highResImageFormat == IMAGE_FORMAT_DXT1) {
        pixbuf = gdk_pixbuf_new(GDK_COLORSPACE_RGB, TRUE, 8, header.width, header.height);
        uint32_t stride = gdk_pixbuf_get_rowstride(pixbuf);
        guchar* pixels = gdk_pixbuf_get_pixels(pixbuf);
        int pos = context->buffer_data_size - ((header.width+3)/4) * ((header.height+3)/4) * 8;
        for (i = 0; i < header.height; i+=4) {
        	for (j = 0; j < header.width; j+=4) {
        	    uint16_t c0 = context->buffer[pos++];
        	    c0 |= context->buffer[pos++] << 8;
        	    
        	    uint16_t c1 = context->buffer[pos++];
        	    c1 |= context->buffer[pos++] << 8;
        	    
        	    uint16_t r[4], g[4], b[4];
        	    
        	    r[0] = (c0 >> 11) & 31;
        	    r[0] = (r[0] << 3) | (r[0] >> 5);
        	    g[0] = (c0 >> 5) & 63;
        	    g[0] = (g[0] << 2) | (g[0] >> 6);
        	    b[0] = (c0 >> 0) & 31;
        	    b[0] = (b[0] << 3) | (b[0] >> 5);
        	    
        	    r[1] = (c1 >> 11) & 31;
        	    r[1] = (r[1] << 3) | (r[1] >> 5);
        	    g[1] = (c1 >> 5) & 63;
        	    g[1] = (g[1] << 2) | (g[1] >> 6);
        	    b[1] = (c1 >> 0) & 31;
        	    b[1] = (b[1] << 3) | (b[1] >> 5);
        	    
        	    uint16_t a[4];
        	    a[0] = 255;
        	    a[1] = 255;
        	    
        	    if(c0 > c1) {
        	        r[2] = (4*r[0] + 2*r[1] + 3)/6;
        	        g[2] = (4*g[0] + 2*g[1] + 3)/6;
        	        b[2] = (4*b[0] + 2*b[1] + 3)/6;
        	        a[2] = 255;
        	        
        	        r[3] = (2*r[0] + 4*r[1] + 3)/6;
        	        g[3] = (2*g[0] + 4*g[1] + 3)/6;
        	        b[3] = (2*b[0] + 4*b[1] + 3)/6;
        	        a[3] = 255;
        	    } else {
        	        r[2] = (r[0] + r[1] + 1)/2;
        	        g[2] = (g[0] + g[1] + 1)/2;
        	        b[2] = (b[0] + b[1] + 1)/2;
        	        a[2] = 255;
        	        
        	        r[3] = 0;
        	        g[3] = 0;
        	        b[3] = 0;
        	        a[3] = 0;
        	    }
        	    
        	    uint32_t sel = context->buffer[pos++];
        	    sel |= context->buffer[pos++] << 8;
        	    sel |= context->buffer[pos++] << 16;
        	    sel |= context->buffer[pos++] << 24;
        	    
        	    int ii, jj;
        	    for(ii = 0; ii < 4; ii++)
            	    for(jj = 0; jj < 4; jj++) {
            	        pixels[stride*(i+ii) + 4*(j+jj) + 0] = r[sel & 3];
            	        pixels[stride*(i+ii) + 4*(j+jj) + 1] = g[sel & 3];
            	        pixels[stride*(i+ii) + 4*(j+jj) + 2] = b[sel & 3];
            	        pixels[stride*(i+ii) + 4*(j+jj) + 3] = a[sel & 3];
                		sel >>= 2;
                    }
        	}
        }
    } else if(header.highResImageFormat == IMAGE_FORMAT_DXT5) {
        pixbuf = gdk_pixbuf_new(GDK_COLORSPACE_RGB, TRUE, 8, header.width, header.height);
        uint32_t stride = gdk_pixbuf_get_rowstride(pixbuf);
        guchar* pixels = gdk_pixbuf_get_pixels(pixbuf);
        int pos = context->buffer_data_size - ((header.width+3)/4) * ((header.height+3)/4) * 16;
        for (i = 0; i < header.height; i+=4) {
        	for (j = 0; j < header.width; j+=4) {
        	    {
            	    uint16_t a[8];
            	    
            	    a[0] = context->buffer[pos++];
            	    a[1] = context->buffer[pos++];
            	    
            	    if(a[0] > a[1]) {
            	        a[2] = (12*a[0] + 2*a[1] + 7)/14;
            	        a[3] = (10*a[0] + 4*a[1] + 7)/14;
            	        a[4] = (8*a[0] + 6*a[1] + 7)/14;
            	        a[5] = (6*a[0] + 8*a[1] + 7)/14;
            	        a[6] = (4*a[0] + 10*a[1] + 7)/14;
            	        a[7] = (2*a[0] + 12*a[1] + 7)/14;
            	    } else {
            	        a[2] = (8*a[0] + 2*a[1] + 5)/10;
            	        a[3] = (6*a[0] + 4*a[1] + 5)/10;
            	        a[4] = (4*a[0] + 6*a[1] + 5)/10;
            	        a[5] = (2*a[0] + 8*a[1] + 5)/10;
            	        a[6] = 0;
            	        a[7] = 255;
            	    }
            	    
            	    uint64_t sel = context->buffer[pos++];
            	    sel |= (uint64_t)context->buffer[pos++] << 8;
            	    sel |= (uint64_t)context->buffer[pos++] << 16;
            	    sel |= (uint64_t)context->buffer[pos++] << 24;
            	    sel |= (uint64_t)context->buffer[pos++] << 32;
            	    sel |= (uint64_t)context->buffer[pos++] << 40;
            	    
            	    int ii, jj;
            	    for(ii = 0; ii < 4; ii++)
                	    for(jj = 0; jj < 4; jj++) {
                	        pixels[stride*(i+ii) + 4*(j+jj)+3] = a[sel & 7];
                    		sel >>= 3;
                        }
                }
                {
            	    uint16_t c0 = context->buffer[pos++];
            	    c0 |= context->buffer[pos++] << 8;
            	    
            	    uint16_t c1 = context->buffer[pos++];
            	    c1 |= context->buffer[pos++] << 8;
            	    
            	    uint16_t r[4], g[4], b[4];
            	    
            	    r[0] = (c0 >> 11) & 31;
            	    r[0] = (r[0] << 3) | (r[0] >> 5);
            	    g[0] = (c0 >> 5) & 63;
            	    g[0] = (g[0] << 2) | (g[0] >> 6);
            	    b[0] = (c0 >> 0) & 31;
            	    b[0] = (b[0] << 3) | (b[0] >> 5);
            	    
            	    r[1] = (c1 >> 11) & 31;
            	    r[1] = (r[1] << 3) | (r[1] >> 5);
            	    g[1] = (c1 >> 5) & 63;
            	    g[1] = (g[1] << 2) | (g[1] >> 6);
            	    b[1] = (c1 >> 0) & 31;
            	    b[1] = (b[1] << 3) | (b[1] >> 5);
            	    
        	        r[2] = (4*r[0] + 2*r[1] + 3)/6;
        	        g[2] = (4*g[0] + 2*g[1] + 3)/6;
        	        b[2] = (4*b[0] + 2*b[1] + 3)/6;
        	        
        	        r[3] = (2*r[0] + 4*r[1] + 3)/6;
        	        g[3] = (2*g[0] + 4*g[1] + 3)/6;
        	        b[3] = (2*b[0] + 4*b[1] + 3)/6;
            	    
            	    uint32_t sel = context->buffer[pos++];
            	    sel |= context->buffer[pos++] << 8;
            	    sel |= context->buffer[pos++] << 16;
            	    sel |= context->buffer[pos++] << 24;
            	    
            	    int ii, jj;
            	    for(ii = 0; ii < 4; ii++)
                	    for(jj = 0; jj < 4; jj++) {
                	        pixels[stride*(i+ii) + 4*(j+jj) + 0] = r[sel & 3];
                	        pixels[stride*(i+ii) + 4*(j+jj) + 1] = g[sel & 3];
                	        pixels[stride*(i+ii) + 4*(j+jj) + 2] = b[sel & 3];
                    		sel >>= 2;
                        }
                }
        	}
        }
    } else {
        printf("Unsupported VTF format - %i.\n", header.highResImageFormat);
    	g_set_error (
    		error,
    		GDK_PIXBUF_ERROR,
    		GDK_PIXBUF_ERROR_CORRUPT_IMAGE,
    		("Can't read this yet."));
        return FALSE;
    }
    
    context->prepared_func(pixbuf, NULL, context->user_data);
    
    return TRUE;
}