#!/usr/bin/ruby

require 'ftools'
require 'fileutils'

$pkg_root = 'zame.GloomyDungeons.freedemo'
$pkg_common = 'zame.GloomyDungeons.common'
$debug_keys = false

def preprocess_file(src_path, dest_path, pkg_root, pkg_curr, build_name, build_code)
	puts "jpp (process): \"#{src_path}\" -> \"#{dest_path}\""

	content = File.open(src_path, 'rb') { |fi| fi.read }

	content.gsub!(/\{PKG_COMMON\}/, $pkg_common.to_s)
	content.gsub!(/\{PKG_ROOT\}/, pkg_root.to_s)
	content.gsub!(/\{PKG_CURR\}/, pkg_curr.to_s)
	content.gsub!(/\{BUILD\}/, build_name.to_s)
	content.gsub!(/\{BUILD_CODE\}/, build_code.to_s)
	content.gsub!(/\{DEBUG_KEYS\}/, $debug_keys.to_s)

	File.open(dest_path, 'wb') { |fo| fo << content }
end

def preprocess_dir(build_name, build_code, src_dir, dest_dir, except_dir=nil, pkg_curr=nil)
	pkg_curr = $pkg_root if pkg_curr.nil?
	FileUtils.mkdir_p(dest_dir) unless File.exists?(dest_dir)

	Dir.open(src_dir).each do |name|
		next if name =~ /^\./

		src_path = "#{src_dir}/#{name}"
		dest_path = "#{dest_dir}/#{name}"

		next if !except_dir.nil? && src_path == except_dir

		if File.directory?(src_path)
			preprocess_dir(build_name, build_code, src_path, dest_path, except_dir, "#{pkg_curr}.#{name}")
			next
		end

		ext = File.extname(src_path)

		if !['.xml', '.java', '.c'].include?(ext)
			puts "jpp (copy): \"#{src_path}\" -> \"#{dest_path}\""
			File.copy(src_path, dest_path)
		elsif ext == '.c'
			preprocess_file(src_path, dest_path, $pkg_root.gsub('.', '_'), pkg_curr.gsub('.', '_'), build_name, build_code)
		else
			preprocess_file(src_path, dest_path, $pkg_root, pkg_curr, build_name, build_code)
		end
	end
end

def preprocess()
	curr_time = Time.now
	build_name = curr_time.strftime('%Y%m%d%H%M%S')
	build_code = curr_time.to_i

	base_dir = File.dirname(__FILE__) + '/..'

	preprocess_dir(
		build_name,
		build_code,
		"#{base_dir}/source/src",
		"#{base_dir}/build/src/" + $pkg_root.gsub('.', '/')
	)

	preprocess_dir(
		build_name,
		build_code,
		"#{base_dir}/source",
		"#{base_dir}/build",
		"#{base_dir}/source/src"
	)
end

preprocess()
