package ru.nsu.fit.usoltsev;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
public class FileInfo  implements Serializable {
    private String fileName;
    private Long fileSize;
}
