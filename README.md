 # webApp
 
This uses scala3, kubernetes, colima, postgresql, cats, doobie, and http4s
to create a simple web service that does not do a ton.

## Running locally

### Colima Install and running

Install [colima](https://github.com/abiosoft/colima) and run it with kubernetes and containerd.  Make sure you *don't*
have Docker desktop running.  If you do, turn it off.  If you want to do this `docker desktop` you are on your own.  The
reason I recommend against `Docker desktop` is because
* It is slow and resource intensive on a Mac
* Licensing can be awkward

To start colima on `MacOS` do something like the below
```shell
colima start --cpu 2 --memory 2 --kubernetes --vm-type=vz --runtime=containerd
```
This says to start a vm with 2 cpus and 2 GB of memory using a containerd runtime with kubernetes support using the vm machine vz.

### Nerdctl Install
`Nerdctl` is a docker compatible cli, but it works with containerd.  To install, run
```shell
colima nerdctl install
```

### Kubectl
`Kubectl` is a cli for talking with kubernetes.  Please follow these [instructions](https://kubernetes.io/docs/tasks/tools/) for your
given OS to install them.

### Build the docker image

Colima requires that *ANY* images that you want kubernetes to see you must use the `nerdctl` namespace k8s.io for it.
```shell
nerdctl build -f docker/Dockerfile -t web-app:latest --progress plain --namespace k8s.io .
```

### Kustomize

[Kustomize](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/) is a tool for overlays for creating
kubernetes infrastructure.  It is built into kubectl.


```shell
kubectl create namespace web-app-dev
kubectl apply -k kube/overlays/local
```




