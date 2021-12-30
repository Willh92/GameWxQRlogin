package com.willh.wz.util.image;

import java.util.Objects;

public class ImageSize {
    public int width;
    public int height;

    public ImageSize() {
    }

    public ImageSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageSize imageSize = (ImageSize) o;
        return width == imageSize.width && height == imageSize.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

}
