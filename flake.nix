{
  inputs = {
    build-gradle-application.url = "github:raphiz/buildGradleApplication";
    # ...
  };

  # ...
  outputs = {
    nixpkgs,
    build-gradle-application,
    ...
    }: {
        # ...
        pkgs = import nixpkgs {
          inherit system;
          overlays = [build-gradle-application.overlays.default];
        };
        # ...
    };
}
