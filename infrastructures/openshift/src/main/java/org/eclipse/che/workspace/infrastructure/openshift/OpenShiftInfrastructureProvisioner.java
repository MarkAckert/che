/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftInternalEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.provision.UniqueNamesProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.env.EnvVarsConverter;
import org.eclipse.che.workspace.infrastructure.openshift.provision.restartpolicy.RestartPolicyRewriter;
import org.eclipse.che.workspace.infrastructure.openshift.provision.route.TlsRouteProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.server.ServersConverter;
import org.eclipse.che.workspace.infrastructure.openshift.provision.volume.PersistentVolumeClaimProvisioner;

/**
 * Applies the set of configurations to the OpenShift environment and environment configuration with
 * the desired order, which corresponds to the needs of the OpenShift infrastructure.
 *
 * @author Anton Korneta
 * @author Alexander Garagatyi
 */
@Singleton
public class OpenShiftInfrastructureProvisioner {

  private final PersistentVolumeClaimProvisioner persistentVolumeClaimProvisioner;
  private final UniqueNamesProvisioner uniqueNamesProvisioner;
  private final TlsRouteProvisioner tlsRouteProvisioner;
  private final ServersConverter serversConverter;
  private final EnvVarsConverter envVarsConverter;
  private final RestartPolicyRewriter restartPolicyRewriter;

  @Inject
  public OpenShiftInfrastructureProvisioner(
      PersistentVolumeClaimProvisioner projectVolumeProvisioner,
      UniqueNamesProvisioner uniqueNamesProvisioner,
      TlsRouteProvisioner tlsRouteProvisioner,
      ServersConverter serversConverter,
      EnvVarsConverter envVarsConverter,
      RestartPolicyRewriter restartPolicyRewriter) {
    this.persistentVolumeClaimProvisioner = projectVolumeProvisioner;
    this.uniqueNamesProvisioner = uniqueNamesProvisioner;
    this.tlsRouteProvisioner = tlsRouteProvisioner;
    this.serversConverter = serversConverter;
    this.envVarsConverter = envVarsConverter;
    this.restartPolicyRewriter = restartPolicyRewriter;
  }

  public void provision(OpenShiftInternalEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    // 1 stage - converting Che model env to OpenShift env
    serversConverter.provision(osEnv, identity);
    envVarsConverter.provision(osEnv, identity);

    // 2 stage - add OpenShift env items
    restartPolicyRewriter.provision(osEnv, identity);
    persistentVolumeClaimProvisioner.provision(osEnv, identity);
    uniqueNamesProvisioner.provision(osEnv, identity);
    tlsRouteProvisioner.provision(osEnv, identity);
  }

  // TODO memory attribute provisioner
}
