SUMMARY = "Graph Visualization Tools"
HOMEPAGE = "http://www.graphviz.org"
LICENSE = "EPL-1.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=9109f5fc16cf963fb3cdd32781b3ce04"

DEPENDS = " \
    intltool-native \
    bison-native \
    groff-native \
    libtool \
    gdk-pixbuf \
    cairo \
    pango \
    expat \
    freetype \
"
DEPENDS:append:class-target = " ${BPN}-native"
DEPENDS:append:class-nativesdk = " ${BPN}-native"

inherit autotools-brokensep pkgconfig gettext

SRC_URI = "https://www2.graphviz.org/Packages/stable/portable_source/${BP}.tar.gz \
           file://0001-plugin-pango-Include-freetype-headers-explicitly.patch \
"
# Use native mkdefs
SRC_URI:append:class-target = "\
           file://0001-Use-native-mkdefs.patch \
           file://0001-Set-use_tcl-to-be-empty-string-if-tcl-is-disabled.patch \
"
SRC_URI:append:class-nativesdk = "\
           file://0001-Use-native-mkdefs.patch \
           file://graphviz-setup.sh \
"
SRC_URI[sha256sum] = "8e1b34763254935243ccdb83c6ce108f531876d7a5dfd443f255e6418b8ea313"

PACKAGECONFIG ??= "librsvg"
PACKAGECONFIG:class-nativesdk ??= ""
PACKAGECONFIG[librsvg] = "--with-librsvg,--without-librsvg,librsvg"

EXTRA_OECONF:append = " PS2PDF=/bin/echo"

EXTRA_OECONF:class-target = "\
                --with-expatincludedir=${STAGING_INCDIR} \
                --with-expatlibdir=${STAGING_LIBDIR} \
                --without-included-ltdl \
                --disable-java \
                --disable-tcl \
                --disable-r \
                --disable-sharp \
                "
EXTRA_OECONF:class-nativesdk = "\
                --with-expatincludedir=${STAGING_INCDIR} \ 
                --with-expatlibdir=${STAGING_LIBDIR} \
                --without-included-ltdl \
                --disable-java \
                --disable-tcl \
                --disable-r \
                --disable-sharp \
                "
CFLAGS:append:class-target = " -D_typ_ssize_t=1 -D_long_double=1"
CFLAGS:append:class-nativesdk = " -D_typ_ssize_t=1 -D_long_double=1"
do_configure:prepend() {
    cd ${S}
    # create version.m4 and ignore libtoolize errors
    ./autogen.sh NOCONFIG || true
}

do_install:append:class-native() {
    # install mkdefs for target build
    install -m755 ${B}/lib/gvpr/mkdefs ${D}${bindir}
}

do_install:append:class-nativesdk() {
    # graphviz-setup.sh must be executed at SDK installation
    install -d ${D}${SDKPATHNATIVE}/post-relocate-setup.d
    install -m 0755 ${WORKDIR}/graphviz-setup.sh ${D}${SDKPATHNATIVE}/post-relocate-setup.d
}
FILES:${PN}:class-nativesdk += "${SDKPATHNATIVE}"

# create /usr/lib/graphviz/config6
graphviz_sstate_postinst() {
    mkdir -p ${SYSROOT_DESTDIR}${bindir}
    dest=${SYSROOT_DESTDIR}${bindir}/postinst-${PN}
    echo '#!/bin/sh' > $dest
    echo '' >> $dest
    echo 'dot -c' >> $dest
    chmod 0755 $dest
}
SYSROOT_PREPROCESS_FUNCS:append:class-native = " graphviz_sstate_postinst"

PACKAGES =+ "${PN}-python ${PN}-perl ${PN}-demo"

FILES:${PN}-python += "${libdir}/python*/site-packages/ ${libdir}/graphviz/python/"
FILES:${PN}-perl += "${libdir}/perl5/*/vendor_perl/ ${libdir}/graphviz/perl/"
FILES:${PN}-demo += "${datadir}/graphviz/demo/"

RDEPENDS:${PN}-perl += "perl"
RDEPENDS:${PN}-python += "python3"
RDEPENDS:${PN}-demo += "python3 perl"

INSANE_SKIP:${PN}-perl = "dev-so"
INSANE_SKIP:${PN}-python = "dev-so"

FILES_SOLIBSDEV:append = " ${libdir}/graphviz/lib*${SOLIBSDEV}"

BBCLASSEXTEND = "native nativesdk"
