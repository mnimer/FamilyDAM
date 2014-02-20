	
// Constructor options block position script 
$.fn.scrollView = function () {
    return this.each(function () {
        var self = this,
        top = $(self).data('top') || $(self).offset().top;
        $(self).data('top',top);
        $('html, body').animate({
            scrollTop: top
        }, 1000);
    })
};

// Custom functions
$(document).ready(function(){
	
	// Ketchup validation script call
	$('#register_form').ketchup();
	$('#quest_form').ketchup();
	
	// Subscribe form script 
	$('#subscribe_form').ketchup().submit(function() {
		if ($(this).ketchup('isValid')) {
			$('#subscribe_submit').button('loading');
			var action = $(this).attr('action');
			$.ajax({
				url: action,
				type: 'POST',
				data: {
					newsletter_email: $('#subscribe_email').val()
				},
				success: function(data) {
					$('#subscribe_submit').button('reset');
                    $('#subscribe_error').fadeOut();
                    $('#subscribe_success').fadeOut();
                    $('#subscribe_success').html(data).fadeIn();
                },
                error: function() {
					$('#subscribe_submit').button('reset');
                    $('#subscribe_error').fadeOut();
                    $('#subscribe_success').fadeOut();
					// Change subscribe form error message text here
                    $('#subscribe_error').html('Oops! Something went wrong!').fadeIn();
                }
			});
		}
		return false;
	});
	
	
	// Question form script
	$('#quest_form').ketchup().submit(function() {
        if ($(this).ketchup('isValid')) {
			$('#quest_form_submit').button('loading');
            var action = $(this).attr('action');
            $.ajax({
                type: "POST",
                url : action,
                data: {
                    contactname: $('#quest_name').val(),
                    contactemail: $('#quest_email').val(),
                    contactmessage: $('#quest_message').val()
                },
                success: function() {
					$('#quest_form_submit').button('reset');
                    $('#quest_error').fadeOut();
                    $('#quest_success').fadeOut();
					// Change question form success message text here
                    $('#quest_success').html('Good question! We will contact you soon! ').fadeIn();
                },
                error: function() {
					$('#quest_form_submit').button('reset');
                    $('#quest_error').fadeOut();
                    $('#quest_success').fadeOut();
					// Change question form error message text here
                    $('#quest_error').html('Oops! Something went wrong!').fadeIn();
                }
            });
        }
        return false;
    })
	
	
    $('.login_link').click(function(event){
        $(this).toggleClass('active').prev().slideToggle();
        event.preventDefault();
    });
    
    // Tabs hide script    
    var tabContainers = $('div.tabs > div');
    tabContainers.hide().filter(':first').show();
    $('div.tabs ul.tabNavigation a').click(function () {
        tabContainers.hide();
        tabContainers.filter(this.hash).show();
        $('div.tabs ul.tabNavigation a').removeClass('selected');
        $(this).addClass('selected');
        return false;
    }).filter(':first').click();
        
	// Fancybox options script	
    $("a.gallery_pic").fancybox({	
        openEffect:'elastic',
        "padding" : 2, // Content padding.
        "frameWidth" : 700,	 // Window width, px (425px - default).
        "frameHeight" : 600, // Window height, px (355px - default).
        "overlayShow" : true, // If "true" - dimming page under pop-up window(default - "true"). The color is defined in jquery.fancybox.css - div # fancy_overlay
        "overlayOpacity" : 0.8,	 // Dimming opacity (default - 0.3).
        "hideOnContentClick" :false, // If "true" - closes the window by clicking on any point (except the navigation controls). Defaul - "true".		
        "centerOnScroll" : false // If "true" - box is centered on the screen when the user scrolls the page.
    });
       
       
    // Constructor preview option block script
    $('.settings_link').toggle(function(event){
        $(this).prev().animate({
            'marginLeft': '-254px'
        },1000);
        event.preventDefault();
    },function(){
        $(this).prev().animate({
            'marginLeft': '0'
        },1000);
    });
    $('.switch a.switch_on').click(function(event){
        $(this).parent().find('b').animate({
            'left': '22px'
        }, 200);
        $(this).parent().removeClass('off').addClass('on');
        var nameBlock = $(this).parent().attr("title");
        $('#' + nameBlock).scrollView();
        $('#' + nameBlock).stop(1,1).delay(1000).fadeIn(); 
        event.preventDefault();
    });
    $('.switch a.switch_off').click(function(event){
        $(this).parent().find('b').animate({
            'left': '1px'
        }, 200);
        $(this).parent().removeClass('on').addClass('off');
        var nameBlock = $(this).parent().attr("title");
        $('#' + nameBlock).scrollView();
        $('#' + nameBlock).stop(1,1).delay(1000).fadeOut();
        event.preventDefault();
    });
    $('.color_type > label').click(function(){
        var checkedRadio = $(this).find('input:checked').attr('class');
        var blockName = $(this).find('input:checked').attr("name");
        if(checkedRadio == 'dark'){
            $('#' + blockName).scrollView().addClass('dark');
        }else{
            $('#' + blockName).scrollView().removeClass('dark');
        } 
    });
});



/* ========================================================================
 * Bootstrap: button.js v3.0.2
 * http://getbootstrap.com/javascript/#buttons
 * ========================================================================
 * Copyright 2013 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ======================================================================== */

+function ($) { "use strict";

  // BUTTON PUBLIC CLASS DEFINITION
  // ==============================

  var Button = function (element, options) {
    this.$element = $(element)
    this.options  = $.extend({}, Button.DEFAULTS, options)
  }

  Button.DEFAULTS = {
    loadingText: 'loading...'
  }

  Button.prototype.setState = function (state) {
    var d    = 'disabled'
    var $el  = this.$element
    var val  = $el.is('input') ? 'val' : 'html'
    var data = $el.data()

    state = state + 'Text'

    if (!data.resetText) $el.data('resetText', $el[val]())

    $el[val](data[state] || this.options[state])

    // push to event loop to allow forms to submit
    setTimeout(function () {
      state == 'loadingText' ?
        $el.addClass(d).attr(d, d) :
        $el.removeClass(d).removeAttr(d);
    }, 0)
  }

  Button.prototype.toggle = function () {
    var $parent = this.$element.closest('[data-toggle="buttons"]')

    if ($parent.length) {
      var $input = this.$element.find('input')
        .prop('checked', !this.$element.hasClass('active'))
        .trigger('change')
      if ($input.prop('type') === 'radio') $parent.find('.active').removeClass('active')
    }

    this.$element.toggleClass('active')
  }


  // BUTTON PLUGIN DEFINITION
  // ========================

  var old = $.fn.button

  $.fn.button = function (option) {
    return this.each(function () {
      var $this   = $(this)
      var data    = $this.data('bs.button')
      var options = typeof option == 'object' && option

      if (!data) $this.data('bs.button', (data = new Button(this, options)))

      if (option == 'toggle') data.toggle()
      else if (option) data.setState(option)
    })
  }

  $.fn.button.Constructor = Button


  // BUTTON NO CONFLICT
  // ==================

  $.fn.button.noConflict = function () {
    $.fn.button = old
    return this
  }


  // BUTTON DATA-API
  // ===============

  $(document).on('click.bs.button.data-api', '[data-toggle^=button]', function (e) {
    var $btn = $(e.target)
    if (!$btn.hasClass('btn')) $btn = $btn.closest('.btn')
    $btn.button('toggle')
    e.preventDefault()
  })

}(jQuery);