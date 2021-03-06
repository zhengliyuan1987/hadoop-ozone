/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.hadoop.ozone.web.ozShell.keys;

import org.apache.hadoop.ozone.OzoneAcl;
import org.apache.hadoop.ozone.client.OzoneClient;
import org.apache.hadoop.ozone.security.acl.OzoneObj;
import org.apache.hadoop.ozone.security.acl.OzoneObjInfo;
import org.apache.hadoop.ozone.web.ozShell.Handler;
import org.apache.hadoop.ozone.web.ozShell.OzoneAddress;
import org.apache.hadoop.ozone.web.ozShell.Shell;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.Objects;

import static org.apache.hadoop.ozone.security.acl.OzoneObj.StoreType.OZONE;

/**
 * Remove acl handler for key.
 */
@Command(name = "removeacl",
    description = "Remove an acl.")
public class RemoveAclKeyHandler extends Handler {

  @Parameters(arity = "1..1", description = Shell.OZONE_BUCKET_URI_DESCRIPTION)
  private String uri;

  @CommandLine.Option(names = {"--acl", "-a"},
      required = true,
      description = "Remove acl." +
          "r = READ," +
          "w = WRITE," +
          "c = CREATE," +
          "d = DELETE," +
          "l = LIST," +
          "a = ALL," +
          "n = NONE," +
          "x = READ_AC," +
          "y = WRITE_AC" +
          "Ex user:user1:rw or group:hadoop:rw")
  private String acl;

  @CommandLine.Option(names = {"--store", "-s"},
      required = false,
      description = "store type. i.e OZONE or S3")
  private String storeType;

  /**
   * Executes the Client Calls.
   */
  @Override
  public Void call() throws Exception {
    Objects.requireNonNull(acl, "ACL to be removed not specified.");
    OzoneAddress address = new OzoneAddress(uri);
    address.ensureKeyAddress();
    try (OzoneClient client =
             address.createClient(createOzoneConfiguration())) {

      String volumeName = address.getVolumeName();
      String bucketName = address.getBucketName();
      String keyName = address.getKeyName();

      if (isVerbose()) {
        System.out.printf("Volume Name : %s%n", volumeName);
        System.out.printf("Bucket Name : %s%n", bucketName);
        System.out.printf("Key Name : %s%n", keyName);
      }

      OzoneObj obj = OzoneObjInfo.Builder.newBuilder()
          .setBucketName(bucketName)
          .setVolumeName(volumeName)
          .setKeyName(keyName)
          .setResType(OzoneObj.ResourceType.KEY)
          .setStoreType(storeType == null ? OZONE :
              OzoneObj.StoreType.valueOf(storeType))
          .build();

      boolean result = client.getObjectStore().removeAcl(obj,
          OzoneAcl.parseAcl(acl));

      String message = result
          ? ("Acl removed successfully.")
          : ("Acl doesn't exist.");

      System.out.println(message);
    }

    return null;
  }

}
