package com.willh.wz.util.image;

import java.util.Objects;

public class ImageTag {

    public String key;
    public ImageSize size;

    public ImageTag(String key, ImageSize size) {
        this.key = key;
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageTag imageTag = (ImageTag) o;
        return Objects.equals(key, imageTag.key) && Objects.equals(size, imageTag.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, size);
    }

}
