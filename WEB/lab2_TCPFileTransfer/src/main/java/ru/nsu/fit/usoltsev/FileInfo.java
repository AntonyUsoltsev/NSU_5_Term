package ru.nsu.fit.usoltsev;

import java.io.Serializable;

public record FileInfo(String fileName, Long fileSize) implements Serializable {
}
