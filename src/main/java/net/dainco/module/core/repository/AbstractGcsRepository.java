package net.dainco.module.core.repository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.dainco.module.core.support.AvroUtils.read;
import static net.dainco.module.core.support.AvroUtils.writeBytes;
import static net.dainco.module.core.support.MorePreconditions.checkNotNullOrEmpty;
import static net.dainco.module.core.support.RxUtils.completeAsyncIo;
import static net.dainco.module.core.support.RxUtils.maybeAsyncIo;
import static net.dainco.module.core.support.RxUtils.observeAsyncIo;
import static net.dainco.module.core.support.RxUtils.singleAsyncIo;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.common.base.Strings;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.io.IOException;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.tika.mime.MimeTypes;

// TODO: Instead of reading the object fully use streams. This was postponed due GCS / Avro stream mapping issues.
public abstract class AbstractGcsRepository<T extends SpecificRecord> {
  private static final String GS_PREFIX = "gs://";

  private final Bucket bucket;
  private final DatumReader<T> datumReader;
  private final DatumWriter<T> datumWriter;
  private final Storage storage;

  public AbstractGcsRepository(Storage storage, String bucketName, Class<T> clazz) {
    bucketName = getBucketName(bucketName);
    this.storage = checkNotNull(storage);
    this.bucket = checkNotNull(storage.get(bucketName), String.format("Bucket [%s] not found.", bucketName));
    this.datumWriter = new SpecificDatumWriter<>(clazz);
    this.datumReader = new SpecificDatumReader<>(clazz);
  }

  public Completable save(T item, String id) {
    return completeAsyncIo(() -> {
      String bucketName = String.format("%s.avro", checkNotNullOrEmpty(id));
      try {
        byte[] bytes = writeBytes(datumWriter, item);
        return bucket.create(bucketName, bytes, MimeTypes.OCTET_STREAM);
      } catch (Exception e) {
        throw new IllegalArgumentException(String.format("Could not write object [%s]: [%s].", id, e.getMessage()), e);
      }
    });
  }

  public Single<Boolean> delete(String filePath) {
    return singleAsyncIo(() -> {
      Blob blob = bucket.get(filePath);
      if (blob != null) {
        return storage.delete(blob.getBlobId());
      }
      return false;
    });
  }

  public Observable<String> list(String folder) {
    String prefix = Strings.isNullOrEmpty(folder) ? "" : folder + "/";
    Storage.BlobListOption option = Storage.BlobListOption.prefix(prefix);
    return observeAsyncIo(() -> bucket.list(option).iterateAll())
        .map(BlobInfo::getName);
  }

  public Maybe<T> readOptional(String path) {
    return maybeAsyncIo(() -> getOrNullBlocking(path));
  }

  private T getOrNullBlocking(String path) throws IOException {
    Blob blob = bucket.get(path);
    if (blob == null) {
      return null;
    }
    return read(datumReader, blob.getContent());
  }

  private String getBucketName(String bucket) {
    checkArgument(Strings.nullToEmpty(bucket).startsWith(GS_PREFIX),
        String.format("Bucket [%s] should start with [%s].", bucket, GS_PREFIX));
    return bucket.substring(GS_PREFIX.length());
  }
}
