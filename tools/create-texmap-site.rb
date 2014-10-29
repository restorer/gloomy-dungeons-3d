#!/usr/bin/ruby

require 'rubygems'
require 'rmagick'

BASE_ICONS = 0x00
BASE_WALLS = 0x10
BASE_TRANSPARENTS = 0x30
BASE_DOORS_F = 0x40
BASE_DOORS_S = 0x44
BASE_OBJECTS = 0x50
BASE_DECORATIONS = 0x48

# convert
# 0x40 .. 0x43 -> 0x34 .. 0x37 (т.е. транспаренты которые а-ля двери)
# 0x50 .. 0x53 -> 0x40 .. 0x43 (двери)
# 0x60 .. 0x63 -> 0x44 .. 0x48 (бока дверей)
# 0x70 .. 0x7F -> 0x50 .. 0x5F
# 0x80 .. 0x87 -> 0x48 .. 0x4F
# 0x88 .. 0x89 -> 0x3E .. 0x3F
# 0x8C .. 0x2F

class TexMapCreator
	def load_texture(result, name, tex)
		xpos = (tex % 16) * 64
		ypos = (tex / 16).floor * 64

		puts name
		img = Magick::ImageList.new(name)

		result.composite!(img, xpos, ypos, Magick::CopyCompositeOp)
	end

	def process(preset)
		base = File.dirname(__FILE__) + "/level-editor/graphics/set-#{preset}"
		base_common = File.dirname(__FILE__) + "/level-editor/graphics/common-#{preset}"
		result = Magick::Image.new(1024, 640).matte_reset!

		load_texture(result, "#{base}/walls/wall_01.png", BASE_WALLS + 0)
		load_texture(result, "#{base}/walls/wall_02.png", BASE_WALLS + 1)
		load_texture(result, "#{base}/walls/wall_03.png", BASE_WALLS + 2)
		load_texture(result, "#{base}/walls/wall_04.png", BASE_WALLS + 3)
		load_texture(result, "#{base}/walls/wall_05.png", BASE_WALLS + 4)
		load_texture(result, "#{base}/walls/wall_06.png", BASE_WALLS + 5)
		load_texture(result, "#{base}/walls/wall_07.png", BASE_WALLS + 6)
		load_texture(result, "#{base}/walls/wall_08.png", BASE_WALLS + 7)
		load_texture(result, "#{base}/walls/wall_09.png", BASE_WALLS + 8)
		load_texture(result, "#{base}/walls/wall_10.png", BASE_WALLS + 9)
		load_texture(result, "#{base}/walls/wall_11.png", BASE_WALLS + 10)
		load_texture(result, "#{base}/walls/wall_12.png", BASE_WALLS + 11)
		load_texture(result, "#{base}/walls/wall_13.png", BASE_WALLS + 12)
		load_texture(result, "#{base}/walls/wall_14.png", BASE_WALLS + 13)
		load_texture(result, "#{base}/walls/wall_15.png", BASE_WALLS + 14)
		load_texture(result, "#{base}/walls/wall_16.png", BASE_WALLS + 15)
		load_texture(result, "#{base}/walls/wall_17.png", BASE_WALLS + 16)
		load_texture(result, "#{base}/walls/wall_18.png", BASE_WALLS + 17)
		load_texture(result, "#{base}/walls/wall_19.png", BASE_WALLS + 18)
		load_texture(result, "#{base}/walls/wall_20.png", BASE_WALLS + 19)
		load_texture(result, "#{base}/walls/wall_21.png", BASE_WALLS + 20)
		load_texture(result, "#{base}/walls/wall_22.png", BASE_WALLS + 21)
		load_texture(result, "#{base}/walls/wall_23.png", BASE_WALLS + 22)
		load_texture(result, "#{base}/walls/wall_24.png", BASE_WALLS + 23)
		load_texture(result, "#{base}/walls/wall_25.png", BASE_WALLS + 24)
		load_texture(result, "#{base}/walls/wall_26.png", BASE_WALLS + 25)
		load_texture(result, "#{base}/walls/wall_27.png", BASE_WALLS + 26)
		load_texture(result, "#{base}/walls/wall_28.png", BASE_WALLS + 27)
		load_texture(result, "#{base}/walls/wall_29.png", BASE_WALLS + 28)
		load_texture(result, "#{base}/walls/wall_30.png", BASE_WALLS + 29)
		load_texture(result, "#{base}/walls/wall_31.png", BASE_WALLS + 30)

		load_texture(result, "#{base}/trans/trans_01.png", BASE_TRANSPARENTS + 0)
		load_texture(result, "#{base}/trans/trans_02.png", BASE_TRANSPARENTS + 1)
		load_texture(result, "#{base}/trans/trans_03.png", BASE_TRANSPARENTS + 2)
		load_texture(result, "#{base}/trans/trans_04.png", BASE_TRANSPARENTS + 3)
		load_texture(result, "#{base}/trans/trans_17.png", BASE_TRANSPARENTS + 4)
		load_texture(result, "#{base}/trans/trans_18.png", BASE_TRANSPARENTS + 5)
		load_texture(result, "#{base}/trans/trans_19.png", BASE_TRANSPARENTS + 6)
		load_texture(result, "#{base}/trans/trans_20.png", BASE_TRANSPARENTS + 7)
		load_texture(result, "#{base}/trans/trans_09.png", BASE_TRANSPARENTS + 8)
		load_texture(result, "#{base}/trans/trans_10.png", BASE_TRANSPARENTS + 9)
		load_texture(result, "#{base}/trans/trans_11.png", BASE_TRANSPARENTS + 10)
		load_texture(result, "#{base}/trans/trans_12.png", BASE_TRANSPARENTS + 11)
		load_texture(result, "#{base}/trans/trans_13.png", BASE_TRANSPARENTS + 12)
		load_texture(result, "#{base}/trans/trans_14.png", BASE_TRANSPARENTS + 13)

		load_texture(result, "#{base}/doors/door_1_f.png", BASE_DOORS_F + 0)
		# load_texture(result, "#{base}/doors/door_2_f.png", BASE_DOORS_F + 1)
		load_texture(result, "#{base}/doors/door_3_f.png", BASE_DOORS_F + 2)
		load_texture(result, "#{base}/doors/door_4_f.png", BASE_DOORS_F + 3)
		load_texture(result, "#{base}/doors/door_5_f.png", BASE_DOORS_F + 1)

		load_texture(result, "#{base}/doors/door_1_s.png", BASE_DOORS_S + 0)
		# load_texture(result, "#{base}/doors/door_2_s.png", BASE_DOORS_S + 1)
		load_texture(result, "#{base}/doors/door_3_s.png", BASE_DOORS_S + 2)
		load_texture(result, "#{base}/doors/door_4_s.png", BASE_DOORS_S + 3)
		load_texture(result, "#{base}/doors/door_5_s.png", BASE_DOORS_S + 1)

		load_texture(result, "#{base_common}/objects/obj_01.png", BASE_OBJECTS + 0)
		load_texture(result, "#{base_common}/objects/obj_02.png", BASE_OBJECTS + 1)
		load_texture(result, "#{base_common}/objects/obj_03.png", BASE_OBJECTS + 2)
		load_texture(result, "#{base_common}/objects/obj_04.png", BASE_OBJECTS + 3)
		load_texture(result, "#{base_common}/objects/obj_05.png", BASE_OBJECTS + 4)
		load_texture(result, "#{base_common}/objects/obj_06.png", BASE_OBJECTS + 5)
		load_texture(result, "#{base_common}/objects/obj_07.png", BASE_OBJECTS + 6)
		load_texture(result, "#{base_common}/objects/obj_08.png", BASE_OBJECTS + 7)
		load_texture(result, "#{base_common}/objects/obj_09.png", BASE_OBJECTS + 8)
		load_texture(result, "#{base_common}/objects/obj_10.png", BASE_OBJECTS + 9)
		load_texture(result, "#{base_common}/objects/obj_11.png", BASE_OBJECTS + 10)
		load_texture(result, "#{base_common}/objects/obj_12.png", BASE_OBJECTS + 11)
		load_texture(result, "#{base_common}/objects/obj_13.png", BASE_OBJECTS + 12)
		load_texture(result, "#{base_common}/objects/obj_14.png", BASE_OBJECTS + 13)
		load_texture(result, "#{base_common}/objects/obj_15.png", BASE_OBJECTS + 14)
		load_texture(result, "#{base_common}/objects/obj_16.png", BASE_OBJECTS + 15)

		load_texture(result, "#{base}/decor/decor_01.png", BASE_DECORATIONS + 0)
		load_texture(result, "#{base}/decor/decor_02.png", BASE_DECORATIONS + 1)
		load_texture(result, "#{base}/decor/decor_03.png", BASE_DECORATIONS + 2)
		load_texture(result, "#{base}/decor/decor_04.png", BASE_DECORATIONS + 3)
		load_texture(result, "#{base}/decor/decor_05.png", BASE_DECORATIONS + 4)
		load_texture(result, "#{base}/decor/decor_06.png", BASE_DECORATIONS + 5)
		load_texture(result, "#{base}/decor/decor_07.png", BASE_DECORATIONS + 6)
		load_texture(result, "#{base}/decor/decor_08.png", BASE_DECORATIONS + 7)
		load_texture(result, "#{base}/decor/decor_09.png", 0x3E)
		load_texture(result, "#{base}/decor/decor_10.png", 0x3F)
		load_texture(result, "#{base}/decor/decor_13.png", 0x2F)

		load_texture(result, "#{base_common}/icons/icon_up.png", BASE_ICONS + 0)
		load_texture(result, "#{base_common}/icons/icon_down.png", BASE_ICONS + 1)
		load_texture(result, "#{base_common}/icons/icon_left.png", BASE_ICONS + 2)
		load_texture(result, "#{base_common}/icons/icon_right.png", BASE_ICONS + 3)
		load_texture(result, "#{base_common}/icons/icon_shoot.png", BASE_ICONS + 4)
		load_texture(result, "#{base_common}/icons/icon_weapon.png", BASE_ICONS + 5)
		load_texture(result, "#{base_common}/icons/icon_rotate_left.png", BASE_ICONS + 6)
		load_texture(result, "#{base_common}/icons/icon_rotate_right.png", BASE_ICONS + 7)
		load_texture(result, "#{base_common}/icons/icon_health.png", BASE_ICONS + 8)
		load_texture(result, "#{base_common}/icons/icon_armor.png", BASE_ICONS + 9)
		load_texture(result, "#{base_common}/icons/icon_ammo.png", BASE_ICONS + 10)
		load_texture(result, "#{base_common}/icons/icon_blue_key.png", BASE_ICONS + 11)
		load_texture(result, "#{base_common}/icons/icon_red_key.png", BASE_ICONS + 12)
		load_texture(result, "#{base_common}/icons/icon_green_key.png", BASE_ICONS + 13)
		load_texture(result, "#{base_common}/icons/icon_map.png", BASE_ICONS + 14)

		result.view(0, 64, 1024, 64 * 9) do |res_view|
			for y in 0 ... (64 * 4 - 1)
				for x in 0 ... 1024
					p = res_view[y][x]
					pp = res_view[y + 64*5][x]

					pp.red = (p.red * 0.625).to_i
					pp.green = (p.green * 0.625).to_i
					pp.blue = (p.blue * 0.625).to_i
					pp.opacity = p.opacity
				end
			end
		end

		result.write(File.dirname(__FILE__) + '/texmap.png')
	end
end

tmc = TexMapCreator.new
tmc.process('normal')
