var Maps = function() {
	var REQUEST_URL = 'maps.php';

	return {
		Init: function() {
			Maps.LoadMapList();
			Maps.InitHandlers();
		},

		InitHandlers: function() {
			jQuery('.button-load').click(function () {
				Maps.LoadMap();
			});

			jQuery('.button-save').click(function () {
				Maps.SaveMap();
			});
			
			jQuery('#wide_screen').click(function () {
				Maps.SetWide();
			});
		},
		
		SetWide: function () {
			if ($('#wide_screen').attr('checked'))
			{
				$('.level-container').css({'width' : '860px'});
				$('#items').css({'left' : '880px'});
				$('.buttons').css({'left' : '880px'});
			}
			else
			{
				$('.level-container').css({'width' : '660px'});
				$('#items').css({'left' : '680px'});
				$('.buttons').css({'left' : '680px'});
			}
		},
		
		LoadMap: function() {
			var map_name = jQuery('.maps').val();
			var data = { mode: 'load_map', name: map_name };

			jQuery.ajax({
				url: REQUEST_URL,
				data: data,
				success: function(data) {
					jQuery('#data-text').val(data);
					do_load();
					jQuery('.current-map-name').val(map_name);
				}
			});
		},

		SaveMap: function() {
			do_save();
			
			var data = {
				mode: 'save_map', 
				map: jQuery('#data-text').val(), 
				name: jQuery('.current-map-name').val() 
			};

			jQuery.ajax({
				url: REQUEST_URL,
				data: data,
				type: 'POST',
				success: function(data) {
					log('[I] ' + data);
				}
			});
		},

		LoadMapList: function ()
		{
			jQuery('.maps').html('');

			jQuery.ajax({
				url: REQUEST_URL,
				data: { mode: 'get_list' },
				dataType: 'json',
				success: function(data) {
					var html = '';

					for (var i = 0; i < data.length; i++) {
						html += '<option value="' + data[i] + '">' + data[i] + '</option>';
					}

					jQuery('.maps').html(html);
				}
			});
		}
	};
}();

jQuery(Maps.Init);
