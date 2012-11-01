    if(header.highResImageFormat == IMAGE_FORMAT_DXT1) {
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