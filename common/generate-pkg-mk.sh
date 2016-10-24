#!/bin/sh

common_dir=$(dirname $0)
base_dir=${common_dir}/..

for pkg in $@
do
    pkg_dir="${base_dir}/${pkg}"
    cat <<MAKE
${common_dir}/lib/${pkg}.stamp: \$(call lookup_scala_srcs, ${pkg_dir}) \$(rocketchip_stamp)
	rm -f ${pkg_dir}/lib
	ln -s ${common_dir}/lib ${pkg_dir}/lib
	cd ${pkg_dir} && \$(SBT) package
	cp ${pkg_dir}/target/scala-2.11/*.jar ${common_dir}/lib
	touch \$@
MAKE
done
