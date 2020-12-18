package net.dainco.module.core.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.file.SeekableInput;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificRecord;

public final class AvroUtils {
  private static final CodecFactory AVRO_CODEC = CodecFactory.snappyCodec();

  public static <T extends SpecificRecord> T read(DatumReader<T> datumReader, byte[] data)
      throws IOException {
    SeekableInput input = new SeekableByteArrayInput(data);
    try (DataFileReader<T> dataFileReader = new DataFileReader<>(input, datumReader)) {
      return dataFileReader.next();
    }
  }

  public static <T extends SpecificRecord> void write(DatumWriter<T> datumWriter, T item, OutputStream out)
      throws IOException {
    DataFileWriter<T> dataFileWriter = new DataFileWriter<>(datumWriter);
    dataFileWriter.setCodec(AVRO_CODEC);
    dataFileWriter.create(item.getSchema(), out);
    dataFileWriter.append(item);
    dataFileWriter.close();
  }

  public static <T extends SpecificRecord> byte[] writeBytes(DatumWriter<T> datumWriter, T item) throws IOException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      write(datumWriter, item, out);
      return out.toByteArray();
    } catch (Exception e) {
      throw new IOException(String.format("Could not write object: [%s].", e.getMessage()), e);
    }
  }
}
