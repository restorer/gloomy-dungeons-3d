#!/usr/bin/ruby

require 'fileutils'

ACTION_CLOSE = 1
ACTION_OPEN = 2
ACTION_REQ_KEY = 3
ACTION_WALL = 4
ACTION_NEXT_LEVEL = 5
ACTION_NEXT_TUTOR_LEVEL = 6
ACTION_DISABLE_PISTOL = 7
ACTION_ENABLE_PISTOL = 8
ACTION_WEAPON_HAND = 9
ACTION_RESTORE_HEALTH = 10
ACTION_SECRET = 11

def convert_level(from_name, actions_name, to_name)
	cont = File.open(from_name, 'rb'){ |fi| fi.read }
	data = cont.gsub(/[^0-9A-Za-z]/, '').scan(/../).map { |v| v.to_i(16) }

	h = data[0]
	w = data[1]

	pos = 4
	level = []

	for i in 1 .. h
		line = []

		for j in 1 .. w
			line << data[pos]
			pos += 1
		end

		level << line
	end

	marks = []
	avail_marks = {}

	for i in 1 .. h
		for j in 1 .. w
			mark = data[pos]
			pos += 1

			unless mark == 0
				avail_marks[mark] = true
				marks << { :mark => mark, :x => j-1, :y => i-1 }
			end
		end
	end

	for i in 1 ... h-1
		for j in 1 ... w-1
			idx = level[i][j]
			vert = (0x10 ... 0x50).include?(level[i-1][j]) && (0x10 ... 0x50).include?(level[i+1][j])

			if (0x50 ... 0x70).include?(idx)		# update door hor/vert state
				idx = 0x50 + (idx % 0x10) + (vert ? 0x10 : 0)
			elsif (0x40 ... 0x50).include?(idx)		# update transparents (not all) hor/vert state
				idx = 0x40 + (idx % 8) + (vert ? 8 : 0)
			end

			level[i][j] = idx
		end
	end

	used_marks = {}
	actions = {}

	if File.exists?(actions_name)
		File.open(actions_name, 'rb') do |fi|
			fi.each_line do |line|
				line = line.strip
				next if line.empty?

				spl = line.split(':').map{ |v| v.strip }.reject{ |v| v.empty? }
				key = (spl.first == '' ? 0 : spl.first.to_i)
				values = spl.last.split(' ').map{ |v| v.strip }.reject{ |v| v.empty? }

				next unless key == 0 || avail_marks.key?(key)

				used_marks[key] = true unless key == 0
				actions[key] = [] unless actions.key?(key)

				pos = 0

				while pos < values.size
					val = values[pos]
					pos += 1

					if ['close', 'open'].include?(val)
						mark = values[pos].to_i
						pos += 1

						if avail_marks.key?(mark)
							used_marks[mark] = true
							actions[key] << (val == 'close' ? ACTION_CLOSE : ACTION_OPEN)
							actions[key] << mark
						end
					elsif ['req_key', 'wall'].include?(val)
						mark = values[pos].to_i
						param = values[pos + 1].to_i
						pos += 2

						if avail_marks.key?(mark)
							used_marks[mark] = true
							actions[key] << (val == 'req_key' ? ACTION_REQ_KEY : ACTION_WALL)
							actions[key] << mark
							actions[key] << param
						end
					elsif val == 'next_level'
						actions[key] << ACTION_NEXT_LEVEL
					elsif val == 'next_tutor_level'
						actions[key] << ACTION_NEXT_TUTOR_LEVEL
					elsif val == 'disable_pistol'
						actions[key] << ACTION_DISABLE_PISTOL
					elsif val == 'enable_pistol'
						actions[key] << ACTION_ENABLE_PISTOL
					elsif val == 'weapon_hand'
						actions[key] << ACTION_WEAPON_HAND
					elsif val == 'restore_health'
						actions[key] << ACTION_RESTORE_HEALTH
					elsif val == 'secret'
						param = values[pos].to_i
						pos += 1

						actions[key] << ACTION_SECRET
						actions[key] << param
					else
						puts "Unknown action \"#{val}\""
					end
				end
			end
		end
	end

	res = [h, w]

	for i in 0 ... h
		for j in 0 ... w
			res << level[i][j]
		end
	end

	marks.each do |item|
		next unless used_marks.key?(item[:mark])

		res << item[:mark]
		res << item[:y]
		res << item[:x]
	end

	res << 255

	if actions.key?(0)
		res << 0
		res += actions[0]
		res << 0
	end

	actions.each do |key, val|
		next if key == 0

		res << key
		res += val
		res << 0
	end

	res << 255

	File.open(to_name, 'wb') { |fo| fo << res.map{ |v| v.chr }.join }
end

def process
	levels_dir = File.dirname(__FILE__) + '/level-editor/levels'
	build_dir = File.dirname(__FILE__) + '/../build/assets/levels'

	FileUtils.mkdir_p(build_dir)

	Dir.open(levels_dir).each do |name|
		next if name =~ /^\./
		next unless name =~ /^level\-\d+\.txt$/

		actions_name = name.gsub(/level/, 'actions')
		res_name = name.gsub(/\.txt$/, '.map')

		convert_level("#{levels_dir}/#{name}", "#{levels_dir}/#{actions_name}", "#{build_dir}/#{res_name}")
		puts "#{name} converted"
	end
end

process
