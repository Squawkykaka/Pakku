# package.nix
{
  lib,
  version,
  buildGradleApplication,
}:
buildGradleApplication {
  pname = "Pakku";
  version = "v${version}";
  src = ./.;
  meta = with lib; {
    description = "A multiplatform modpack manager for Minecraft: Java Edition.";
  };
}
