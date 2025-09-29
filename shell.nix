{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = with pkgs; [
    jdk17
    gradle
    android-tools
  ];

  shellHook = ''
    export JAVA_HOME=${pkgs.jdk17}
    export ANDROID_HOME=$HOME/Android
    export PATH=$ANDROID_HOME/platform-tools:$PATH
  '';
}
