#!/usr/bin/ruby

require 'fileutils'

$pkg_root = ''
$pkg_common = 'zame.GloomyDungeons.oscommon'
$admob_unit_id = 'XXXXXXXX'
$values_hash = {}
$exclude_hash = {}
$include_libs = []

def preprocess_file(src_path, dest_path, pkg_root, pkg_curr, build_name, build_code, ext)
	puts "jpp (process): \"#{src_path}\" -> \"#{dest_path}\""

	content = File.open(src_path, 'rb') { |fi| fi.read }

	content.gsub!(/\{\s*\$PKG_COMMON\s*\}/, $pkg_common.to_s)
	content.gsub!(/\{\s*\$ADMOB_UNIT_ID\s*\}/, $admob_unit_id.to_s)
	content.gsub!(/\{\s*\$PKG_ROOT\s*\}/, pkg_root.to_s)
	content.gsub!(/\{\s*\$PKG_CURR\s*\}/, pkg_curr.to_s)
	content.gsub!(/\{\s*\$BUILD\s*\}/, build_name.to_s)
	content.gsub!(/\{\s*\$BUILD_CODE\s*\}/, build_code.to_s)

	lines = content.split("\n")
	content = ''
	comments_stack = []

	lines.each do |line|
		line = line.chomp

		is_directive = false
		was_changed = false
		prev = (comments_stack.last || false)

		if ext == '.xml'
			mt = line.match(/^\s*<!--\s*#if\s+([a-zA-Z0-9_|! ]+)-->\s*$/)
		else
			mt = line.match(/^\s*\/\/\s*#if\s+([a-zA-Z0-9_|! ]+)$/)
		end

		if !mt.nil?
			line = "#if #{mt[1].strip}"
			is_directive = true
			val = false

			mt[1].gsub(/\s/, '').split('|').each do |item|
				if item[0, 1] == '!'
					val ||= !$values_hash.key?(item[1, item.size])
				else
					val ||= $values_hash.key?(item)
				end
			end

			val = !val || prev
			was_changed = (val != prev)
			comments_stack << val
		else
			if ext == '.xml'
				mt = line.match(/^\s*<!--\s*#end\s*-->\s*$/)
			else
				mt = line.match(/^\s*\/\/\s*#end\s*$/)
			end

			if !mt.nil?
				line = '#end'
				is_directive = true

				if comments_stack.empty?
					puts "jpp (error): #end without #if"
				else
					comments_stack.pop
					was_changed = ((comments_stack.last || false) != prev)
				end
			end
		end

		if was_changed && comments_stack.last
			content += (ext == '.xml' ? '<!--' : '/*') + ' ' + line
		elsif was_changed && !comments_stack.last
			content += line + ' ' + (ext == '.xml' ? '-->' : '*/')
		elsif comments_stack.last
			if ext == '.xml'
				content += line.gsub('<!--', '< ! - -').gsub('-->', '- - >')
			else
				content += line.gsub('/*', '/ *').gsub('*/', '* /')
			end
		elsif is_directive
			if ext == '.xml'
				content += "<!-- #{line} -->"
			else
				content += "/* #{line} */"
			end
		else
			content += line
		end

		content += "\n"
	end

	File.open(dest_path, 'wb') { |fo| fo << content }
end

def preprocess_dir(build_name, build_code, src_dir, dest_dir, except_dir=nil, pkg_curr=nil)
	pkg_curr = $pkg_root if pkg_curr.nil?
	FileUtils.mkdir_p(dest_dir) unless File.exists?(dest_dir)

	Dir.open(src_dir).each do |name|
		next if name =~ /^\./
		next if $exclude_hash.key?(name)

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
			FileUtils.copy(src_path, dest_path)
		elsif ext == '.c'
			preprocess_file(src_path, dest_path, $pkg_root.gsub('.', '_'), pkg_curr.gsub('.', '_'), build_name, build_code, ext)
		else
			preprocess_file(src_path, dest_path, $pkg_root, pkg_curr, build_name, build_code, ext)
		end
	end
end

def preprocess
	curr_time = Time.now
	build_name = curr_time.strftime('%Y.%m.%d.%H%M')
	build_code = curr_time.to_i

	base_dir = File.dirname(__FILE__) + '/../..'

	preprocess_dir(
		build_name,
		build_code,
		"#{base_dir}/source/src",
		"#{base_dir}/.build/src/" + $pkg_root.gsub('.', '/')
	)

	preprocess_dir(
		build_name,
		build_code,
		"#{base_dir}/source",
		"#{base_dir}/.build",
		"#{base_dir}/source/src"
	)
end

def fix_libs(add_mode_only=false)
	base_dir = File.dirname(__FILE__) + '/../..'
	dest_dir = "#{base_dir}/.build/libs"

	return unless File.directory?(dest_dir)

	src_dir = "#{base_dir}/source/libs"

	if !add_mode_only && File.directory?(src_dir)
		Dir.open(src_dir).each do |name|
			next if name =~ /^\./
			next if $exclude_hash.key?(name)

			src_path = "#{src_dir}/#{name}"
			dest_path = "#{dest_dir}/#{name}"

			puts "jpp (copy): \"#{src_path}\" -> \"#{dest_path}\""
			FileUtils.copy_entry(src_path, dest_path)
		end
	end

	src_dir = File.dirname(__FILE__) + '/additional-libs'

	if File.directory?(src_dir)
		$include_libs.each do |name|
			src_path = "#{src_dir}/#{name}"
			dest_path = "#{dest_dir}/#{name}"

			dest_path_dir = File.dirname(dest_path)
			FileUtils.mkdir_p(dest_path_dir) unless File.exists?(dest_path_dir)

			puts "jpp (copy): \"#{src_path}\" -> \"#{dest_path}\""
			FileUtils.copy(src_path, dest_path)
		end
	end
end

def parse_args
	fix_libs_mode = false

	while !ARGV.empty?
		arg = ARGV.shift.downcase

		if arg == '-d'
			unless ARGV.empty?
				$values_hash[ARGV.shift] = true
			end
		elsif arg == '-x'
			unless ARGV.empty?
				$exclude_hash[ARGV.shift] = true
			end
		elsif arg == '-i'
			unless ARGV.empty?
				$include_libs << ARGV.shift
			end
		elsif arg == '-pr'
			unless ARGV.empty?
				$pkg_root = ARGV.shift
			end
		elsif arg == '--fixlibs'
			fix_libs_mode = true
		end
	end

	if $pkg_root == ''
		puts 'Package root is not specified'
		return
	end

	if fix_libs_mode
		fix_libs
	else
		preprocess
		fix_libs(true)
	end
end

parse_args
