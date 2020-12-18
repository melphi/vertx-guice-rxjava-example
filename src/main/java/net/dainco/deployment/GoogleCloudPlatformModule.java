package net.dainco.deployment;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class GoogleCloudPlatformModule extends AbstractModule {
  @Provides
  public Storage storage() {
    return StorageOptions.getDefaultInstance()
        .getService();
  }
}
